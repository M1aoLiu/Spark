import jwt
import websocket

try:
    import thread
except ImportError:
    import _thread as thread
import json
import time


'''
参照虎牙开发者文档获取实时弹幕信息
'''


def get_sign(app_id, secret):
    """
    传入开发者app_id与密钥，返回加密的token
    :param app_id: 开发者的app_id
    :param secret: 开发者密钥
    :return:
    """
    payload = {
        "iat": int(time.time()),
        "exp": int(time.time()) + 600,
        "appId": app_id
    }
    # 使用HS256进行加密，获取所需token
    token = jwt.encode(payload, secret, algorithm='HS256')
    return token


def islegal(name):
    flag = True
    for _char in name[:]:
        if not ('\u4e00' <= _char <= '\u9fa5' or
                'a' <= _char <= 'z' or 'A' <= _char <= 'Z' or '1' < _char <= '9'):
            print("false")
            return False
    print("true")
    return flag


def getMsg(ws, info):
    if 'badgeName' in info['data'] and info['notice'] == 'getMessageNotice':
        print("raw msg:", info)
        msg_list.append(info)
        if len(msg_list) >= num:
            f = open('a/barrage.txt', 'a',
                     encoding='utf-8')
            curr_time = str(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime()))
            for i in msg_list:
                if islegal(i['data']['sendNick']) and islegal(i['data']['content']):
                    msg = ''
                    msg += str(i['data']['roomId']) + '\t'  # 房间号
                    msg += '虎牙\t'  # 直播平台
                    msg += i['data']['sendNick'] + '\t'  # 发送者名称
                    msg += str(i['data']['unionId']) + '\t'  # 用户id
                    msg += i['data']['content'] + '\t'  # 消息内容
                    msg += str(i['data']['senderLevel']) + '\t'  # 发送者等级
                    msg += curr_time  # 时间戳
                    print('detail msg:', msg)
                    f.write(msg + '\n')
            f.close()
            msg_list.clear()


def on_message(ws, message):
    info = json.loads(message)
    getMsg(ws, info)


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("### closed ###")
    ws.close()

def on_open(ws):
    print("on_open")
    def run(*args):
        ws.send(
            '{"command":"subscribeNotice",'
            '"data":["getMessageNotice","getSendItemNotice"],'
            '"reqId":"123456789"}')
        # 保持心跳，10s发送一次
        while True:
            ws.send("ping")
            time.sleep(10)
    thread.start_new_thread(run, ())


def get_conn():
    sign = get_sign(app_id, secret)
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp(
        "ws://ws-apiext.huya.com/index.html?do=comm&roomId=" + room_id + "&appId=" + app_id + "&iat=" + str(
            int(time.time())) + "&sToken=" + sign,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close)
    ws.on_open = on_open
    ws.run_forever()


if __name__ == "__main__":

    msg_list = []
    totalNum = 0
    room_id = "521000"
    num = 20
    app_id = 'w5183a0b95da8d72'
    secret = '024f204a25b56cd36541f962f40ebe9b'
    get_conn()