# Falcon

A framework with which to remotely load classes into a minecraft forge mod's classpath.

## Use cases

- Updating classes without requiring manual downloads from the user
- Downloading classes based on some sort of server authentication

## Usage

1. Clone the repo
2. Copy your mod source into falcon's source
3. Rename any instances of com/yourclient to your actual base mod package
4. Upload client.jar to your server. Update the url in Loader.kt with the url of the uploaded jar
5. Distribute loader.jar to your users

## Next steps

If you are planning on using remote loading as a security measure, I recommend using obfuscation as well. I recommend checking out binscure (https://discord.gg/MaqN7gA), and obfuscator that I help develop.
