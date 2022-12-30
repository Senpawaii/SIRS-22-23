import modules.gui as gui
import modules.comms as comms

def run():
    # print("This is the client source code!")

    # Start the application GUI
    gui.printPrompt()
    # srv_connection = comms.connect_to_backoffice()

    while(True):
        username, hashed_password = gui.authenticationPrompt()
        print(hashed_password)
        
        token = comms.verify_credentials(username, hashed_password)
        if token:
            while(True):
                action = gui.printSelectionMenu(username)
                match action:
                    case "A1": # Request Sensor Key
                        print("Action A1 selected.")
                    case "A2": # Query Stock
                        print("Action A2 selected.")
                    case "A3": # Buy production parts
                        print("Action A3 selected.")
                    case Any:
                        print("Any: Action {a} selected.".format(a=action))
        else:
            print("Incorrect credentials. Please try again.")