A simple mod that allows you to configure default components for items

You can configure default components for all items (named `global` on the config) and per item (named `perItem` on the config)

Currently the configs on the server and client should match for better experience, and any change to the config requires a restart

The mod is currently on beta and the config may change in the future

An example of a config file:
```json5 default_components.json5
{
    // Components that will apply to all items [REQUIRES RESTART]
    "global": {
        "minecraft:food": {
            saturation: 1,
            nutrition: 1
        },
        "lore": ['""', '"\\u00a77This is a default component"']
    },
    // Components that will apply to specific items [REQUIRES RESTART]
    "perItem": {
        "minecraft:piston": {
            "lore": ['""', '"\\u00a77This is another component"']
        }
    }
}

```
