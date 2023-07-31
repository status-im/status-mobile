# Setup your Editor/IDE

You can use any Clojure enabled editor. Here are instructions for developing the status-mobile app using:

* IntelliJ IDEA and Cursive
* VS Code and Calva
* Emacs/Cider

(Please add instructions for your favorite editor.)

## Table of Contents

- [Using Cursive](#using-cursive)
  - [Install Cursive](#install-cursive)
  - [Getting Cursive to understand `status-mobile`](#getting-cursive-to-understand-status-mobile)
  - [I get a lot of `cannot be resolved`](#i-get-a-lot-of-cannot-be-resolved)
  - [REPL!](#repl)
    - [Connecting to REPL to IntelliJ](#connecting-to-repl-to-intellij)
    - [Connecting REPL and IntelliJ to `status-mobile`](#connecting-repl-and-intellij-to-status-mobile)
- [Using Calva](#using-calva)
  - [Start and connect the REPL](#start-and-connect-the-repl)
  - [Use the REPL](#use-the-repl)

## Using Cursive

### Install Cursive

See https://cursive-ide.com/userguide/index.html

### Getting Cursive to understand `status-mobile`

- Add this file to the root of the `status-mobile` project dir
  - https://gist.github.com/Samyoul/f71a0593ba7a12d24dd0d5ef986ebbec
- Right click and "add as leiningen project"

<img src="images/ide-setup/1_fake_project_file.png" width=75% />

## I get a lot of `cannot be resolved`

Are you getting problems where you get a lot of `cannot be resolved` on everything?

<img src="images/ide-setup/2_resolve.jpeg" width=75% />

See https://cursive-ide.com/userguide/macros.html

- opt+enter (on macOS)
- resolve defview as fn and letsubs as let
- move selection on resolve and hit enter
- and select defn for defview and let for letsubs

### REPL!

#### Connecting to REPL to IntelliJ

I had a number of problems connecting to REPL, the solution is as follows:

At the top of IntelliJ IDEA click on the `Add Configuration...` option:

<img src="images/ide-setup/3_REPL_1.png" width=75% />

This will load the following menu:

<img src="images/ide-setup/4_REPL_2.png" width=75% />

Click on the `+` icon in the top left corner of the menu.

Select `Clojure REPL > Remote`

<img src="images/ide-setup/5_REPL_3.png" width=75% />

Which will load the following menu

<img src="images/ide-setup/6_REPL_4.png" width=75% />

Enter the below options:

- Name = status-mobile
- Display Name = status-mobile
- Connection type = nREPL
- Connection details
  - Host = 127.0.0.1
  - Port = 7888

<img src="images/ide-setup/7_REPL_5.png" width=75% />

Press `OK`

Now the below option will be visible.
Press the green run button

<img src="images/ide-setup/8_REPL_6.png" width=75% />

You should now see an dialog with the following message:

```shell
Connecting to remote nREPL server...
Clojure 1.10.1
```

To confirm you have a working connection with REPL enter the following command in the input box below the output:

```shell
(prn 1)
```

Which should output

```shell
(prn 1)
1
=> nil
```

See below:

<img src="images/ide-setup/9_REPL_7.png" width=75% />

#### Connecting REPL and IntelliJ to `status-mobile`

**The important next step is telling REPL the context in which to interact with the code.**

Do the following:

Ensure you have 3 terminals running the following

- `make run-clojure`
- `make run-metro`
- `make run-ios` / `make run-android`

[See the STARTING GUIDE for details](STARTING_GUIDE.md#development)

Next go back to the REPL input and enter the following commands:

```clojure
(shadow/watch :mobile)
(shadow/repl :mobile)
```

See Below

<img src="images/ide-setup/10_REPL_8.png" width="75%" />

Which should switch the clj file type target to cljs as shown above

Finally you are ready to test REPL.

Create a sample function to evaluate something simple like `(prn "I'm working")`, move your cursor to one of the outer parentheses. Right or `control` click and select the `REPL` option. From there select `Sync files in REPL` and then `Send '...' to REPL'`.

<img src="images/ide-setup/11_REPL_9.png" width="75%" />

Alternatively you can use the shortcut commands `⇧⌘M` to sync your files and `⇧⌘P` to send the statement to REPL. You may also need to switch the REPL namespace to match the current file, which can be done manually from the dialogue box or using the `⇧⌘N` shortcut key.

Following the above should give you the below result:

<img src="images/ide-setup/12_REPL_10.png" width="75%" />

🎉 Tada! Working! 🎉

---

For additional details on issues you may face when setting up REPL with Cursive [see this document](https://notes.status.im/9Gr7kqF8SzC_SmYK0eB7uQ?view#Connecting-Cursive--IntelliJ-IDEA-to-REPL-Problems)

## Using Calva

For VS Code users.

0. Install Calva.

### Start and connect the REPL

1. Open the `status-mobile` folder.
1. Start [Status development](starting-guide.md#development) (Starting the `run-clojure` and `run-metro` jobs in split view in the VS Code integrated terminal works great.)
1. Run the VS Code command: **Calva: Connect to a running REPL Server in the project**
   1. Select the project type `shadow-cljs`
   1. Accept the suggested connection `host:port`
   1. Select to connect to the `:mobile` build

### Use the REPL

Open any `.cljs` file in the project and evaluate forms in it. See https://calva.io/try-first/ for some starter tips and links. Confirm that your REPL is connected to the app by evaluating:

```clojure
(js/alert "Hello from Status App!")
```

🎉 Tada! You are ready to use the REPL to improve Status.im! 🎉

Please consider bookmarking [calva.io](https://calva.io/) for quick access to the Calva documentation. 

## Using Emacs/Cider

1. Install Emacs/Cider/etc. (there is a lot of variability in how to manage things in emacs, so please google for help with this)
2. Add a local ~/.shadow-cljs/config.edn file like below (corresponding to the version numbers of the packages you are using):

```clojure
{:dependencies
 [[nrepl/nrepl "0.9.0"]
  [cider/cider-nrepl "0.28.4"]
  [cider/piggieback "0.5.2"]]}
```


