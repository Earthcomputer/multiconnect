from java.lang import Integer

all_tests = []


class Test:
    def __init__(self, func, min_protocol, max_protocol):
        self.func = func
        self.min_protocol = min_protocol
        self.max_protocol = max_protocol


def test(min_protocol=0, max_protocol=Integer.MAX_VALUE):
    def decorator(test_func):
        all_tests.append(Test(test_func, min_protocol, max_protocol))
        return test_func
    return decorator


def get_all_tests():
    return all_tests
