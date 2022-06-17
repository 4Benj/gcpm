# GCPM - Grasscutter Permission Manager
GCPM is a [Grasscutter](https://github.com/Grasscutters/Grasscutter) permission manager plugin for server administrators
to enhance the current permission system by adding permission groups.

## Currently Planned Features:
- [x] Permission Groups
- [ ] Player Prefixes
- [ ] Flexible Commands

The features listed are to achieve an MVP for the first release.

## Important Notes:
This plugin is made to run on the current [Development](https://github.com/Grasscutters/Grasscutter/tree/development) branch of Grasscutter. \
This plugin is in very early development and only manually adding permission groups via MongoDB is supported. \
**If you require support please ask on the [Grasscutter Discord](https://discord.gg/T5vZU6UyeG). However, support is not guaranteed.** \
**If you encounter any issues, please report them on the [issue tracker](https://github.com/4Benj/gcpm/issues). However, please search to see if anyone else has encountered your issue before. Any duplicate issues will be closed.**
<h3 style="margin:0;padding:0;">THE ISSUE TRACKER IS NOT A SUPPORT FORM.</h3>

## Setup
### Download Plugin Jar
Coming soon!

### Compile yourself
1. Pull the latest code from github using ``git clone https://github.com/4Benj/gcpm`` in your terminal of choice.
2. In the newly created gcpm folder run ``gradlew build`` (cmd) **or** ``./gradlew build`` (Powershell, Linux & Mac).
3. Assuming the build succeeded, copy the ``gcpm-plugin-1.0.0-dev.jar`` file, copy it.
4. Navigate to your ``Grasscutter`` server, find the ``plugins`` folder and paste the ``gcpm-plugin-1.0.0-dev.jar`` into it.
5. Start your server.
6. Profit?

Your final plugins folder directory structure should look similar to this
```
plugins
│   gcpm-plugin-1.0.0-dev.jar
│   ...
└───GCPM
    │   config.json
    │   groups.json
```