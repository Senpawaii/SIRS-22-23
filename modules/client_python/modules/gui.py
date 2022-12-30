import modules.security as SIRSsecurity

def printPrompt():
    print("==================================")
    print("Welcome to StarDrive Systems")
    print("==================================")
    print("")

def authenticationPrompt():
    print("Please authenticate yourself: ")
    username = SIRSsecurity.get_username()
    hashed_password = SIRSsecurity.get_hash_password()
    return username, hashed_password
    
def printSelectionMenu(username):
    while(True):
        print("-----------------------------------")
        print("Hello " + username + ", how can we help you today?")
        print("")
        print("1 - Contact BackOffice")
        print("2 - Contact FrontOffice")
        print("3 - Contact Actuators/Sensors")
        print("4 - Quit")
        print("")
        command = input("Please select an option: ")
        match command:
            case "1":
                while(True):
                    command = printBackofficeMenu()
                    if(command in {"1","2","3"}):
                        return "A"+command
                    elif(command == "4"):
                        break
                    print("Please insert a valid command.")
            case "2":
                print("Contact FrontOffice")
                # comms.contactFrontoffice()
            case "3":
                print("Contact Actuators")
            case "4":
                print("Goodbye!")
                exit(0)
            case Any:
                print("Please insert a valid command.")

def printBackofficeMenu():
    print("-----------------------------------")
    print("=BackOffice Menu=")
    print("-----------------------------------")
    print("")
    print("1 - Request Sensor Key")
    print("2 - Query stock")
    print("3 - Buy Parts")
    print("4 - Back")
    print("")
    return input("Please select an option: ")
