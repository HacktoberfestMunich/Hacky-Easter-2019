import math
import random
import socket
import time
from enum import Enum

import numpy
from PIL import Image


class Direction(Enum):
    UP = 1
    DOWN = 2
    LEFT = 3
    RIGHT = 4


class FlutServer:

    def __init__(self, ip, port):
        self._ip = ip
        self._port = port
        self._connected = False
        self.connect()
        self.x, self.y = self.get_size()

    def connect(self):
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.settimeout(1.0)
        self._sock.connect((self._ip, self._port))
        self._connected = True

    def disconnect(self):
        if self._connected:
            self._sock.close()
            self._connected = False

    def set_pixel(self, x, y, r, g, b, a=255):
        if not self._connected:
            print("Error: Not connected!")
            return
        if a == 255:
            self._sock.send(('PX %d %d %02x%02x%02x\n' % (x, y, r, g, b)).encode())
        else:
            self._sock.send(('PX %d %d %02x%02x%02x%02x\n' % (x, y, r, g, b, a)).encode())

    def _receive_message(self):
        message = b''
        char = self._sock.recv(1)
        while char != b'\n':
            message += char
            char = self._sock.recv(1)
        values = message.decode().split(' ')[1:]
        x = int(values[0])
        y = int(values[1])
        if len(values) > 2:
            return tuple(int(values[2][i:i + 2], 16) for i in (0, 2, 4, 6))
        else:
            return (x, y)

    def get_pixel(self, x, y):
        if not self._connected:
            print("Error: Not connected!")
            return
        width, height = self.get_size()
        if x > width - 1 or y > height - 1:
            print(x, y)
            self._sock.send(('PX %d %d\n' % (width - 1, height - 1)).encode())
            return self._receive_message()

        self._sock.send(('PX %d %d\n' % (x, y)).encode())
        return self._receive_message()

    def get_size(self):
        if not self._connected:
            print("Error: Not connected!")
            return
        self._sock.send('SIZE\n'.encode())
        return self._receive_message()


class GeometricDrawer():
    def __init__(self, flutServer):
        self._fs = flutServer

    def draw_bubbles(self, number):
        size = self._fs.get_size()
        for _ in range(number):
            x = random.randint(0, size[0])
            y = random.randint(0, size[1])
            radius = random.randint(20, 200)
            self.draw_circle(x, y, radius, 255, 255, 255)

    def draw_rectangle(self, x0, y0, x, y, r, g, b, a=255):
        for i in range(x):
            for j in range(y):
                self._fs.set_pixel(x0 + i, y0 + j, r, g, b, a)

    def draw_circle(self, x0, y0, radius, r, g, b, a=255):
        f = 1 - radius
        ddf_x = 1
        ddf_y = -2 * radius
        x = 0
        y = radius
        self._fs.set_pixel(x0, y0 + radius, r, g, b, a)
        self._fs.set_pixel(x0, y0 - radius, r, g, b, a)
        self._fs.set_pixel(x0 + radius, y0, r, g, b, a)
        self._fs.set_pixel(x0 - radius, y0, r, g, b, a)

        while x < y:
            if f >= 0:
                y -= 1
                ddf_y += 2
                f += ddf_y
            x += 1
            ddf_x += 2
            f += ddf_x
            self._fs.set_pixel(x0 + x, y0 + y, r, g, b, a)
            self._fs.set_pixel(x0 - x, y0 + y, r, g, b, a)
            self._fs.set_pixel(x0 + x, y0 - y, r, g, b, a)
            self._fs.set_pixel(x0 - x, y0 - y, r, g, b, a)
            self._fs.set_pixel(x0 + y, y0 + x, r, g, b, a)
            self._fs.set_pixel(x0 - y, y0 + x, r, g, b, a)
            self._fs.set_pixel(x0 + y, y0 - x, r, g, b, a)
            self._fs.set_pixel(x0 - y, y0 - x, r, g, b, a)


class MazeSolver():


    def __init__(self, flutServer):
        self._fs = flutServer
        self._start_color = (0, 255, 255, 255)
        self._goal_color = (255, 0, 255, 255)

    def is_wall(self, x, y):
        colors = self._fs.get_pixel(x, y)
        if colors == (255, 255, 255, 255):
            return True
        else:
            return False

    def find_entry(self):
        result = []
        x_size, y_size = self._fs.get_size()
        x_start = int(x_size / 3)
        x_end = int((x_size / 3) * 2)
        y_start = int(y_size / 3)
        y_end = int((y_size / 3) * 2)

        for x in range(x_start, x_end):
            if not self.is_wall(x, y_start):
                result.append((x, y_start))
            if not self.is_wall(x, y_end):
                result.append((x, y_end))

        for y in range(y_start, y_end):
            if not self.is_wall(x_start, y):
                result.append((x_start, y))
            if not self.is_wall(x_end, y):
                result.append((x_end, y))

        if len(result):
            result.sort(key=lambda tup: tup[0])
            x_mid = result[int(len(result) / 2)][0]
            result.sort(key=lambda tup: tup[1])
            y_mid = result[int(len(result) / 2)][1]
            return (x_mid, y_mid)

    def find_start(self):
        x = self._fs.x // 3
        y = self._fs.y // 3
        for i in range(30):
            if self._fs.get_pixel(x + i, y + i) == self._start_color:
                x += i
                y += i
                x -= self._find_wall(x, y, Direction.LEFT) - 1
                y -= self._find_wall(x, y, Direction.UP) - 1
                return x, y
        print("Error: No start found")

    def _find_wall(self, x, y, direction):
        if direction == Direction.UP:
            if y == self._fs.y // 3:
                return 0
            for i in range(y - self._fs.y // 3):
                if self.is_wall(x, y - i):
                    return i
        elif direction == Direction.DOWN:
            if y == self._fs.y // 3 * 2:
                return 0
            for i in range(self._fs.y // 3 * 2 - y):
                if self.is_wall(x, y + i):
                    return i
        elif direction == Direction.LEFT:
            if x == self._fs.x // 3:
                return 0
            for i in range(x - self._fs.x // 3):
                if self.is_wall(x - i, y):
                    return i
        elif direction == Direction.RIGHT:
            if x == self._fs.x // 3 * 2:
                return 0
            for i in range(self._fs.x // 3 * 2 - x):
                if self.is_wall(x + i, y):
                    return i

    def _draw_step(self, x, y, direction):
        if direction == Direction.DOWN:
            for i in range(18):
                self._fs.set_pixel(x, y + i, 255, 0 ,0)
                self._fs.set_pixel(x + 1, y + i, 255, 0 ,0)
        elif direction == Direction.LEFT:
            for i in range(18):
                self._fs.set_pixel(x - i, y, 255, 0 ,0)
                self._fs.set_pixel(x - i, y + 1, 255, 0 ,0)
        elif direction == Direction.UP:
            for i in range(18):
                self._fs.set_pixel(x, y - i, 255, 0 ,0)
                self._fs.set_pixel(x + 1, y - i, 255, 0 ,0)
        elif direction == Direction.RIGHT:
            for i in range(18):
                self._fs.set_pixel(x + i, y, 255, 0 ,0)
                self._fs.set_pixel(x + i, y + 1, 255, 0 ,0)

    def _do_right_hand_step(self, x, y, direction):
        if self._fs.get_pixel(x,y) == self._goal_color:
            print("Solved")
            return None
        if direction == Direction.DOWN:
            if self._find_wall(x, y, Direction.DOWN) > 17:
                self._draw_step(x, y, Direction.DOWN)
                y += 18
                return (x, y, Direction.LEFT)
            else:
                return (x, y, Direction.RIGHT)
        elif direction == Direction.LEFT:
            if self._find_wall(x, y, Direction.LEFT) > 17:
                self._draw_step(x, y, Direction.LEFT)
                x -= 18
                return (x, y, Direction.UP)
            else:
                return (x, y, Direction.DOWN)
        elif direction == Direction.UP:
            if self._find_wall(x, y, Direction.UP) > 17:
                self._draw_step(x, y, Direction.UP)
                y -= 18
                return (x, y, Direction.RIGHT)
            else:
                return (x, y, Direction.LEFT)
        elif direction == Direction.RIGHT:
            if self._find_wall(x, y, Direction.RIGHT) > 17:
                self._draw_step(x, y, Direction.RIGHT)
                x += 18
                return (x, y, Direction.DOWN)
            else:
                return (x, y, Direction.UP)

    def solve_right_hand(self):
        x, y = self.find_start()
        direction = Direction.DOWN
        while True:
            try:
                x, y, direction = self._do_right_hand_step(x,y,direction)
            except:
                break

class PictureDrawer():

    def __init__(self, flutServer):
        self._fs = flutServer

    def draw_picture(self, x0, y0, x, y, path, ignore_white=False):
        im = Image.open(path)
        im.thumbnail((x, y))
        x, y = im.size
        for i in range(x):
            for j in range(y):
                r, g, b = im.getpixel((i, j))
                if not ignore_white or (r, g, b) != (255, 255, 255):
                    self._fs.set_pixel(x0 + i, y0 + j, r, g, b)

    def draw_animation(self, x0, y0, x, y, xt, yt, s, path):
        im = Image.open(path)
        im.thumbnail((x, y))
        x, y = im.size
        dist = int(math.sqrt(xt * xt + yt * yt))

        cache = numpy.empty((x + xt, y + yt), dtype=(int, 4))
        isset = numpy.zeros((x + xt, y + yt))
        for i in range(x + xt):
            for j in range(y + yt):
                cache[i][j] = self._fs.get_pixel(x0 + i, y0 + j)

        for d in range(dist):
            xi = int(xt * (d / dist))
            yi = int(yt * (d / dist))
            for i in range(x):
                for j in range(y):
                    r, g, b = im.getpixel((i, j))
                    if (r, g, b) != (255, 255, 255):
                        self._fs.set_pixel(x0 + xi + i, y0 + yi + j,
                                                   r, g, b)
                        isset[xi + i][yi + j] = 1
                    else:
                        if isset[xi + i][yi + j] == 1:
                            self._fs.set_pixel(x0 + xi + i, y0 + yi + j,
                                                       *cache[xi + i][yi + j][:3])
                            isset[xi + i][yi + j] = 0
            time.sleep(1.0 / s)


class GameBoard():

    def __init__(self, flutServer):
        self._fs = flutServer
        self.x, self.y = self._fs.get_size()

    def get_field(self, x, y):
        return (int((self.x / 6) * x) + 1,
                int((self.y / 6) * y) + 1,
                int(self.x / 6) - 2 + ((x + 1) % 2),
                int(self.y / 6) - 2 + ((y + 1) % 2))

    def get_random_field(self):
        x = 3
        y = 3
        while x > 1 and x < 4 and y > 1 and y < 4:
            x = random.randint(0, 5)
            y = random.randint(0, 5)
        return self.get_field(x, y)


class Util():

    def random_color(self):
        return (random.randint(0, 255),
                random.randint(0, 255),
                random.randint(0, 255),
                random.randint(0, 255))


def main():
    fs = FlutServer('127.0.0.1', 1234)
    gb = GameBoard(fs)
    ms = MazeSolver(fs)
    gd = GeometricDrawer(fs)
    pd = PictureDrawer(fs)

    #x, y = ms.find_entry()
    #gd.draw_bubbles(100)
    #gd.draw_circle(x, y, 20, 255, 255, 255, 255)

    #x, y, xw, yw = gb.get_random_field()
    #pd.draw_picture(x, y, xw, yw, "troll.jpg")
    #pd.draw_animation(x + 30, y, 300, 300,  0, 80, 20, "thug.jpg")

    ms.solve_right_hand()
    # x,y = ms.find_start()
    # print(ms._find_wall(x,y,Direction.DOWN))

if __name__ == "__main__":
    main()
