import re
import getpass
import hashlib

""" 
    Sanitization module. It is crucial that this sanitization is executed again in the servers.
    Sanitizing data on the client side, allows for the mitigation of unnecessary requests. 
"""

def get_username():
    while(True):
        username = input("Username: ")

        # Check if the username:
        #   - is not empty;
        #   - is not something that is not a string 
        #   - matches upper/lower characters or numbers
        #   - fixed size under or equal 10 characters 
        if not username or not isinstance(username, str) or not re.match(r"^[a-zA-Z0-9]{1,10}$", username):
            print("Invalid username. Please try again.")
        else:
            return username

def get_hash_password():
    # TODO: Add some mechanism to truncate received password size on the SERVER ONLY, THIS APP IS THE CLIENTS PROBLEM IF HE MESSES WITH IT. 
    # Inputing a 32KB password creates considerable lag on the client, let alone the server.
    password = getpass.getpass()

    # Check if the password:
        #   - is not empty;
        #   - is not something that is not a string 
        #   - matches upper/lower characters or numbers and special characters

    return hashlib.sha256(password.encode()).hexdigest()