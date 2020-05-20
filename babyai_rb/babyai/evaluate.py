import numpy as np
import gym


# Returns the performance of the agent on the environment for a particular number of episodes.
def evaluate(agent, env, episodes, model_agent=True, offsets=None):
    # Initialize logs
    if model_agent:
        agent.model.eval()
    logs = {"num_frames_per_episode": [], "return_per_episode": [], "observations_per_episode": []}

    if offsets:
        count = 0

    for i in range(episodes):
        if offsets:
            # Ensuring test on seed offsets that generated successful demonstrations
            while count != offsets[i]:
                obs = env.reset()
                count += 1

        obs = env.reset()
        agent.on_reset()
        done = False

        num_frames = 0
        returnn = 0
        obss = []
        while not done:
            action = agent.act(obs)['action']
            obss.append(obs)
            obs, reward, done, _ = env.step(action)
            agent.analyze_feedback(reward, done)
            num_frames += 1
            returnn += reward


        logs["observations_per_episode"].append(obss)
        logs["num_frames_per_episode"].append(num_frames)
        logs["return_per_episode"].append(returnn)
    if model_agent:
        agent.model.train()
    return logs


def evaluate_demo_agent(agent, episodes):
    logs = {"num_frames_per_episode": [], "return_per_episode": []}

    number_of_demos = len(agent.demos)

    for demo_id in range(min(number_of_demos, episodes)):
        logs["num_frames_per_episode"].append(len(agent.demos[demo_id]))

    return logs


class ManyEnvs(gym.Env):

    def __init__(self, envs, rbs, rb_prop=1):
        self.envs = envs
        self.rbs = rbs
        self.done = [False] * len(self.envs)
        self.rb_prop = rb_prop

    def seed(self, seeds):
        [env.seed(seed) for seed, env in zip(seeds, self.envs)]

    def reset(self):
        many_obs = [env.reset() for env in self.envs]
        for rb in self.rbs:
            if rb:
                rb.reset()
        self.done = [False] * len(self.envs)
        return many_obs

    def rb_step(self, res, rb):
        obs, reward, done, info = res
        if rb:
            rb.transition(obs)
            if done:
                rb_reward = rb.get_reward()
                if self.rb_prop:
                    rb_reward *= reward * self.rb_prop
                reward += rb_reward
        return obs, reward, done, info

    def step(self, actions):
        self.results = [self.rb_step(env.step(action), rb) if not done else self.last_results[i]
                        for i, (env, action, done, rb)
                        in enumerate(zip(self.envs, actions, self.done, self.rbs))]
        self.done = [result[2] for result in self.results]
        self.last_results = self.results
        return zip(*self.results)

    def render(self):
        raise NotImplementedError


# Returns the performance of the agent on the environment for a particular number of episodes.
def batch_evaluate(agent, env_name, seed, episodes, return_obss_actions=False, rb=None, bolt_state=False, rb_prop = 1):
    num_envs = min(256, episodes)

    envs = []
    rbs = []
    for i in range(num_envs):
        env = gym.make(env_name)
        envs.append(env)

        if not rb:
            rbs.append(None)
        elif rb == "SimpleBallVisit":
            from babyai.rl.rb import SimpleBallVisitRestrainingBolt
            rbs.append(SimpleBallVisitRestrainingBolt())
        elif rb == "ObjectsVisitRestrainingBolt":
            from babyai.rl.rb import ObjectsVisitRestrainingBolt
            rbs.append(ObjectsVisitRestrainingBolt())
        elif rb == "VisitAndPickRestrainingBolt":
            from babyai.rl.rb import VisitAndPickRestrainingBolt
            rbs.append(VisitAndPickRestrainingBolt())
        else:
            raise ValueError("Incorrect restraining bolt name: {}".format(rb))

    env = ManyEnvs(envs, rbs, rb_prop)

    logs = {
        "num_frames_per_episode": [],
        "return_per_episode": [],
        "observations_per_episode": [],
        "actions_per_episode": [],
        "seed_per_episode": []
    }

    for i in range((episodes + num_envs - 1) // num_envs):
        seeds = range(seed + i * num_envs, seed + (i + 1) * num_envs)
        env.seed(seeds)

        many_obs = env.reset()

        cur_num_frames = 0
        num_frames = np.zeros((num_envs,), dtype='int64')
        returns = np.zeros((num_envs,))
        already_done = np.zeros((num_envs,), dtype='bool')
        if return_obss_actions:
            obss = [[] for _ in range(num_envs)]
            actions = [[] for _ in range(num_envs)]
        while (num_frames == 0).any():
            action = agent.act_batch(many_obs, rbs=rbs)['action']
            if return_obss_actions:
                for i in range(num_envs):
                    if not already_done[i]:
                        obss[i].append(many_obs[i])
                        actions[i].append(action[i].item())
            many_obs, reward, done, _ = env.step(action)
            agent.analyze_feedback(reward, done)
            done = np.array(done)
            just_done = done & (~already_done)
            returns += reward * just_done
            cur_num_frames += 1
            num_frames[just_done] = cur_num_frames
            already_done[done] = True

        logs["num_frames_per_episode"].extend(list(num_frames))
        logs["return_per_episode"].extend(list(returns))
        logs["seed_per_episode"].extend(list(seeds))
        if return_obss_actions:
            logs["observations_per_episode"].extend(obss)
            logs["actions_per_episode"].extend(actions)

    return logs
