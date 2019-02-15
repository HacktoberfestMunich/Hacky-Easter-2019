import socket
import random

class FlutServer:

    def __init__(self, ip, port):
        self._ip = ip
        self._port = port
        self._connected = False

    def connect(self):
        self._sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._sock.connect((self._ip, self._port))
        self._connected = True

    def disconnect(self):
        if self._connected:
            self._sock.close()

    def set_pixel(self, x, y, r, g, b, a=255):
        if not self._connected:
            print("Error: Not connected!")
            return
        if a == 255:
            self._sock.send(('PX %d %d %02x%02x%02x\n' % (x,y,r,g,b)).encode())
        else:
            self._sock.send(('PX %d %d %02x%02x%02x%02x\n' % (x,y,r,g,b,a)).encode())

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
            return tuple(int(values[2][i:i+2], 16) for i in (0, 2, 4, 6))
        else:
            return (x, y)

    def get_pixel(self, x, y):
        if not self._connected:
            print("Error: Not connected!")
            return
        self._sock.send(('PX %d %d\n' % (x,y)).encode())
        return self._receive_message()

    def get_size(self):
        if not self._connected:
            print("Error: Not connected!")
            return
        self._sock.send('SIZE\n'.encode())
        return self._receive_message()

class PixelDrawer():
    def __init__(self, ip, port):
        self._flutServer = FlutServer('127.0.0.1', 1234)
        self._flutServer.connect()

    def draw_bubbles(self, number):
        size = self._flutServer.get_size()
        for _ in range(number):
            x = random.randint(0, size[0])
            y = random.randint(0, size[1])
            radius = random.randint(20, 200)
            self.draw_circle(x, y, radius, 255, 255, 255)

    def draw_circle(self, x0, y0, radius, r, g, b, a=255):
        f = 1 - radius
        ddf_x = 1
        ddf_y = -2 * radius
        x = 0
        y = radius
        self._flutServer.set_pixel(x0, y0 + radius, r, g, b, a)
        self._flutServer.set_pixel(x0, y0 - radius, r, g, b, a)
        self._flutServer.set_pixel(x0 + radius, y0, r, g, b, a)
        self._flutServer.set_pixel(x0 - radius, y0, r, g, b, a)

        while x < y:
            if f >= 0:
                y -= 1
                ddf_y += 2
                f += ddf_y
            x += 1
            ddf_x += 2
            f += ddf_x
            self._flutServer.set_pixel(x0 + x, y0 + y, r, g, b, a)
            self._flutServer.set_pixel(x0 - x, y0 + y, r, g, b, a)
            self._flutServer.set_pixel(x0 + x, y0 - y, r, g, b, a)
            self._flutServer.set_pixel(x0 - x, y0 - y, r, g, b, a)
            self._flutServer.set_pixel(x0 + y, y0 + x, r, g, b, a)
            self._flutServer.set_pixel(x0 - y, y0 + x, r, g, b, a)
            self._flutServer.set_pixel(x0 + y, y0 - x, r, g, b, a)
            self._flutServer.set_pixel(x0 - y, y0 - x, r, g, b, a)

class MazeSolver():

    def __init__(self, ip, port):
        self._flutServer = FlutServer('127.0.0.1', 1234)
        self._flutServer.connect()

    def is_wall(self, x, y):
        colors = self._flutServer.get_pixel(x, y)
        if colors == (255, 255, 255, 255):
            return True
        else:
            return False

    def find_entry(self):
        result = []
        x_size, y_size = self._flutServer.get_size()
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
            x_mid = result[int(len(result)/2)][0]
            result.sort(key=lambda tup: tup[1])
            y_mid = result[int(len(result)/2)][1]
            return (x_mid, y_mid)

def main():
    ms = MazeSolver('127.0.0.1', 1234)
    x, y = ms.find_entry()
    pd = PixelDrawer('127.0.0.1', 1234)
    pd.draw_circle(x, y, 20, 255, 255, 255, 255)

if __name__ == "__main__":
    main()
