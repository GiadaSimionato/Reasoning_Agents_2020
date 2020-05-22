from abc import ABC, abstractmethod

class RestrainingBolt(ABC):

    def __init__(self, num_states, final_states,
                 initial_state=0, reward=1):
        self.num_states = num_states
        self.reward = reward
        self.initial_state = initial_state
        self.final_states = final_states
        self.reset()

    def reset(self):
        self.current_state = self.initial_state

    @abstractmethod
    def transition(self, world_state):
        pass

    def get_reward(self):
        if self.current_state in self.final_states:
            return self.reward
        else:
            return 0

class FluentRestrainingBolt(RestrainingBolt):

    def __init__(self, num_states, final_states,
                 initial_state=0, reward=0.5):
       super().__init__(num_states, final_states, initial_state, reward)

    def transition(self, world_state):
        self.compute_fluents(world_state)
        self.compute_transition()

    @abstractmethod
    def compute_transition(self):
        pass

    @abstractmethod
    def compute_fluents(self, world_state):
        pass

    def reset(self):
        super().reset()
        self.current_fluents = set()

class MultiBolt():

    def __init__(self, bolts, same_rewards=True):
        self.bolts = bolts
        self.same_rewards = same_rewards
        self.reset()

    def reset(self):
        for bolt in self.bolts:
            bolt.reset()

    def transition(self, world_state):
        for bolt in self.bolts:
            bolt.transition(world_state)
        
    def get_reward(self):
        reward = 0
        for bolt in self.bolts:
            r = bolt.get_reward()
            if self.same_rewards:
                reward += r * 1/len(self.bolts)
            else:
                reward += r
        return reward

    @property
    def current_state(self):
        state = self.bolts[0].current_state
        for bolt in self.bolts[1:]:
            state += bolt.current_state * bolt.num_states
        return state

# -------------------- Baby Data --------------------

class BabyColor():
    RED = 0
    GREEN = 1
    BLUE = 2
    VIOLET = 3
    YELLOW = 4
    GREY = 5

class BabyObject():
    EMPTY = 0
    FLOOR = 1
    WALL = 2
    DOOR = 4
    KEY = 5
    BALL = 6
    BOX = 7

class BabyData():
    COLOR = BabyColor()
    OBJECT = BabyObject()
    FRONT_X = 3
    FRONT_Y = -2

# ------------------  test bolts --------------------


class FluentSimpleBallVisitRestrainingBolt(FluentRestrainingBolt):
    # FLUENTS: "at_blue_ball"
    NUM_STATES = 2
    FINAL_STATES = [1]   

    def __init__(self):
        super().__init__(num_states=FluentSimpleBallVisitRestrainingBolt.NUM_STATES,
                         final_states=FluentSimpleBallVisitRestrainingBolt.FINAL_STATES)

    def compute_transition(self):
        if "at_blue_ball" in self.current_fluents:
            self.current_state = 1
            
    def compute_fluents(self, world_state):
        obs = world_state["image"]
        if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
            [BabyData.OBJECT.BALL, BabyData.COLOR.BLUE, 0]).all():
            self.current_fluents.add("at_blue_ball")
        elif "at_blue_ball" in self.current_fluents:
            self.current_fluents.remove("at_blue_ball")


class SimpleBallVisitRestrainingBolt(RestrainingBolt):
    NUM_STATES = 2
    FINAL_STATES = [1]   

    def __init__(self):
        super().__init__(num_states=SimpleBallVisitRestrainingBolt.NUM_STATES,
                         final_states=SimpleBallVisitRestrainingBolt.FINAL_STATES)

    def transition(self, world_state):
        obs = world_state["image"]
        # at_blue_ball
        if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
            [BabyData.OBJECT.BALL, BabyData.COLOR.BLUE, 0]).all():
            self.current_state = 1


# ----------------- ambiguity bolts ------------------

class ObjectsVisitRestrainingBolt(RestrainingBolt):
    NUM_STATES = 3
    FINAL_STATES = [2]   

    def __init__(self):
        super().__init__(num_states=ObjectsVisitRestrainingBolt.NUM_STATES,
                         final_states=ObjectsVisitRestrainingBolt.FINAL_STATES)

    def transition(self, world_state):
        obs = world_state["image"]
        # at_grey_ball
        if self.current_state == 0:
            if ((obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
            [BabyData.OBJECT.BALL, BabyData.COLOR.GREY, 0]).all()):
                self.current_state = 1
        # at_grey_key
        if self.current_state == 1:
            if ((obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
            [BabyData.OBJECT.KEY, BabyData.COLOR.GREY, 0]).all()):
                self.current_state = 2

class ObjectsVisitSeparateRestrainingBolt(RestrainingBolt):
    NUM_STATES = 4
    FINAL_STATES = [3]  

    def __init__(self):
        super().__init__(num_states=ObjectsVisitSeparateRestrainingBolt.NUM_STATES,
                            final_states=ObjectsVisitSeparateRestrainingBolt.FINAL_STATES)

    def transition(self, world_state):
        obs = world_state["image"]
        if self.current_state == 0:
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
                [BabyData.OBJECT.BALL, BabyData.COLOR.GREY, 0]).all():
                self.current_state = 1
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
                [BabyData.OBJECT.KEY, BabyData.COLOR.GREY, 0]).all():
                self.current_state = 2
        elif self.current_state == 1:
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
                [BabyData.OBJECT.KEY, BabyData.COLOR.GREY, 0]).all():
                self.current_state = 3
        elif self.current_state == 2:
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, :] == 
                [BabyData.OBJECT.BALL, BabyData.COLOR.GREY, 0]).all():
                self.current_state = 3


# ----------------- second level -------------------


class OpenBoxRestrainingBolt(RestrainingBolt):
    NUM_STATES = 3
    FINAL_STATES = [0]  

    def __init__(self):
        super().__init__(num_states=OpenBoxRestrainingBolt.NUM_STATES,
                            final_states=OpenBoxRestrainingBolt.FINAL_STATES)
        self.last_direction = -1

    def transition(self, world_state):
        direction = world_state["direction"]
        obs = world_state["image"]
        if self.current_state == 0:
            if obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] == BabyData.OBJECT.BOX:
                self.current_state = 1
        elif self.current_state == 1:
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] == BabyData.OBJECT.FLOOR
                and self.last_direction == direction):
                self.current_state = 0
            elif obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] != BabyData.OBJECT.BOX:
                self.current_state = 2
        self.last_direction = direction

class VisitBoxRestrainingBolt(RestrainingBolt):
    NUM_STATES = 2
    FINAL_STATES = [1]  

    def __init__(self):
        super().__init__(num_states=VisitBoxRestrainingBolt.NUM_STATES,
                            final_states=VisitBoxRestrainingBolt.FINAL_STATES)

    def transition(self, world_state):
        obs = world_state["image"]
        if self.current_state == 0:
            if obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] == BabyData.OBJECT.BOX:
                self.current_state = 1

class VisitBoxAndPickMultiRestrainingBolt(MultiBolt):

    def __init__(self):
        super().__init__([VisitBoxRestrainingBolt(), OpenBoxRestrainingBolt()])

class VisitAndPickRestrainingBolt(RestrainingBolt):
    NUM_STATES = 3
    FINAL_STATES = [2]   

    def __init__(self):
        super().__init__(num_states=VisitAndPickRestrainingBolt.NUM_STATES,
                         final_states=VisitAndPickRestrainingBolt.FINAL_STATES)
        self.last_direction = -1

    def transition(self, world_state):
        direction = world_state["direction"]
        obs = world_state["image"]
        if self.current_state == 0:
            if obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] == BabyData.OBJECT.BOX:
                self.current_state = 1
        elif self.current_state == 1:
            if (obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] == BabyData.OBJECT.FLOOR
                and self.last_direction == direction):
                self.current_state = 2
            elif obs[BabyData.FRONT_X, BabyData.FRONT_Y, 0] != BabyData.OBJECT.BOX:
                self.current_state = 0
        self.last_direction = direction
