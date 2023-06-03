{
  "variables": {
    "module_name%": "status_nodejs_addon",
    "module_path%": "./lib/binding/"
  },
  "targets": [{
    "target_name": "<(module_name)",
    "sources": [
      "./modules/react-native-status/nodejs/status.cpp"
    ],
    "xcode_settings": {
      "MACOSX_DEPLOYMENT_TARGET": "10.7"
    },
    "libraries": [
      "<!(pwd)/result/libstatus.a"
    ],
    "conditions": [
      ["OS=='mac'", {
        "libraries": [
          "-framework IOKit",
          "-framework CoreFoundation",
          "-framework CoreServices",
          "-framework Security"
        ]
      }]
    ]
  }, {
    "target_name": "action_after_build",
    "type": "none",
    "dependencies": ["<(module_name)"],
    "copies": [
      {
        "files": [ "<(PRODUCT_DIR)/<(module_name).node" ],
        "destination": "<(module_path)"
      }
    ]
  }]
}
