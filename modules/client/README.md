## Creating a virtual environment
Can be useful when multiple projects use different version of the same package, for example
```bash
py -m venv project_development
```
Activate (turn on) the virtual environment.
```bash
project_development/Scripts/activate
```

## Install required packages
```bash
python -m pip install -r requirements.txt
```

## Installing python packages
>NOTE: Remember to only install packages while using the virtual environment.

Use to install a package.
```bash
py -m pip install "SomeProject"
```
Or, if you prefer, install a specific version of a package.
```bash
py -m pip install "SomeProject==1.4"
```

## Uninstalling python packages
Use to uninstall a package.
```bash
pip uninstall "SomeProject"
```

## Backup all necessary packages in requirements.txt file
To make sure we are all using the same packages, remember to update the requirements file after adding/deleting packages.
```bash
python -m pip freeze > requirements.txt
```

## Running main project
```bash
python -m client
```

## Turning off the virtual environment
```bash
deactivate
```