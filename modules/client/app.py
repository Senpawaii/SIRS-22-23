import modules.gui as gui
import modules.comms as comms

def run():
    # print("This is the client source code!")

    # Start the application GUI
    gui.printPrompt()
    username, hashed_password = gui.authenticationPrompt()
    print(hashed_password)
    while(True):
        if(True):
        
        # if(verify_credentials(username, hashed_password)):
            while(True):
                gui.printSelectionMenu(username)
                command = input("Please select an option: ")
                match command:
                    case "1":
                        comms.contactBackoffice()
                    case "2":
                        comms.contactFrontoffice()
                    case "3":
                        print("Contact Actuators")
                    case "4":
                        print("Goodbye!")
                        exit(0)
                    case Any:
                        print("Please insert a valid command.")
        else:
            print("Incorrect credentials. Please try again.")