from multiprocessing import Process, Pipe
import gym
import time
from babyai.rl.rb import SimpleBallVisitRestrainingBolt

def worker(conn, env, rb, rb_prop):
    while True:
        cmd, data = conn.recv()
        if cmd == "step":
            obs, reward, done, info = env.step(data)
            rb_reward = 0
            if rb:
                rb.transition(obs)
            if done:
                obs = env.reset()
                if rb:
                    rb_reward = rb.get_reward()
                    if rb_prop:
                        rb_reward *= reward
                    rb.reset()
            conn.send((obs, reward + rb_reward, rb_reward, reward, done, info))
        elif cmd == "reset":
            obs = env.reset()
            if rb:
                rb.reset()
            conn.send(obs)
        else:
            raise NotImplementedError

class ParallelEnv(gym.Env):
    """A concurrent execution of environments in multiple processes."""

    def __init__(self, envs, rbs, rb_prop):
        assert len(envs) >= 1, "No environment given."
        self.envs = envs
        self.observation_space = self.envs[0].observation_space
        self.action_space = self.envs[0].action_space
        self.rbs = rbs
        self.rb_prop = rb_prop

        self.locals = []
        self.processes = []
        for env, rb in zip(self.envs[1:], self.rbs[1:]):
            local, remote = Pipe()
            self.locals.append(local)
            p = Process(target=worker, args=(remote, env, rb, rb_prop))
            p.daemon = True
            p.start()
            remote.close()
            self.processes.append(p)

    def reset(self):
        for local in self.locals:
            local.send(("reset", None))
        if self.rbs[0]:
            self.rbs[0].reset()
        results = [self.envs[0].reset()] + [local.recv() for local in self.locals]
        return results

    def step(self, actions):
        for local, action in zip(self.locals, actions[1:]):
            local.send(("step", action))
        obs, reward, done, info = self.envs[0].step(actions[0])
        # print(self.envs[0])
        rb_reward = 0
        if self.rbs[0]:
            self.rbs[0].transition(obs)
        self.envs[0].render()
        if done:
            obs = self.envs[0].reset()
            if self.rbs[0]:
                rb_reward = self.rbs[0].get_reward()
                if self.rb_prop:
                    rb_reward *= reward
                self.rbs[0].reset()
            # print(reward)
        results = zip(*[(obs, reward + rb_reward, rb_reward, reward, done, info)] +
                       [local.recv() for local in self.locals])
        return results

    def render(self):
        raise NotImplementedError

    def __del__(self):
        for p in self.processes:
            p.terminate()