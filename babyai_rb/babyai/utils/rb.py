def load_bolt(name):
    if not name:
        return None
        # args.bolt_state = False
    elif name == "SimpleBallVisit":
        from babyai.rl.rb import SimpleBallVisitRestrainingBolt
        return SimpleBallVisitRestrainingBolt()
    elif name == "ObjectsVisitRestrainingBolt":
        from babyai.rl.rb import ObjectsVisitRestrainingBolt
        return ObjectsVisitRestrainingBolt()
    elif name == "ObjectsVisitSeparateRestrainingBolt":
        from babyai.rl.rb import VisitBoxAndPickMultiRestrainingBolt
        return VisitBoxAndPickMultiRestrainingBolt()
    elif name == "VisitAndPickRestrainingBolt":
        from babyai.rl.rb import VisitAndPickRestrainingBolt
        return VisitAndPickRestrainingBolt()
    elif name == "VisitBoxAndPickMultiRestrainingBolt":
        from babyai.rl.rb import VisitBoxAndPickMultiRestrainingBolt
        return VisitBoxAndPickMultiRestrainingBolt()
    elif name == "VisitBoxRestrainingBolt":
        from babyai.rl.rb import VisitBoxAndPickMultiRestrainingBolt
        return VisitBoxAndPickMultiRestrainingBolt()
    else:
        raise ValueError("Incorrect restraining bolt name: {}".format(name))