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
    print("-----------------------------------")
    print("Hello " + username + ", how can we help you today?")
    print("")
    print("1 - Contact BackOffice")
    print("2 - Contact FrontOffice")
    print("3 - Contact Actuators/Sensors")
    print("4 - Quit")
    print("")


