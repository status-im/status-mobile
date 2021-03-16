# Using IntelliJ IDEA and Cursive

## Install Cursive

See https://cursive-ide.com/userguide/index.html

## Getting Cursive to understand `status-react`

- Add this file to the root of the `status-react` project dir
  - https://gist.github.com/Samyoul/f71a0593ba7a12d24dd0d5ef986ebbec
- Right click and "add as leiningen project"

![](https://notes.status.im/uploads/upload_32ea1a8eeb410effc24b5f3914e38627.png)

## I get a lot of `cannot be resolved`

Are you getting problems where you get a lot of `cannot be resolved` on everything?

![](https://notes.status.im/uploads/upload_73ed33ce7af35a289c88afdb01fc91b7.jpg)

See https://cursive-ide.com/userguide/macros.html

- opt+enter (on macOS)
- resolve defview as fn and letsubs as let
- move selection on resolve and hit enter
- and select defn for defview and let for letsubs

## REPL!

### Connecting to REPL to IntelliJ

I had a number of problems connecting to REPL, the solution is as follows:

At the top of IntelliJ IDEA click on the `Add Configuration...` option:

![](https://notes.status.im/uploads/upload_b16dd48e03ef4cc98c53cc896a532f90.png)

This will load the following menu:

![](https://notes.status.im/uploads/upload_0f50285b745a00cfe3cab3c4af415d21.png)

Click on the `+` icon in the top left corner of the menu.

Select `Clojure REPL > Remote`

![](https://notes.status.im/uploads/upload_0d890d5ed6e2d8d48d31cab7fa99d9e9.png)

Which will load the following menu

![](https://notes.status.im/uploads/upload_563dce472426f1f691a55713556a9b36.png)

Enter the below options:

- Name = status-react
- Display Name = status-react
- Connection type = nREPL
- Connection details
  - Host = 127.0.0.1
  - Port = 7888

![](https://notes.status.im/uploads/upload_88ed1d279c314d2264405f15a5e56433.png)

Press `OK`

Now the below option will be visible.
Press the green run button

![](https://notes.status.im/uploads/upload_d3c538110f5cc21770311b288ac382d7.png)

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

Or

```shell
prn 1
```

Which should output

```shell
prn 1
=> 1
=> #function[clojure.core/prn]
```

See below:

![](https://notes.status.im/uploads/upload_0f39b27d0ad287db7a5f1f180f9c55b5.png)

### Connecting REPL and IntelliJ to `status-react`

**The important next step is telling REPL the context in which to interact with the code.**

Do the following:

Ensure you have 3 terminals running the following

- `make run-clojure`
- `make run-metro`
- `make run-ios`

// TODO Add the details of selecting the
```clojure
(shadow/watch :mobile)
(shadow/repl :mobile)
```
- switch clj to cljs
- You do need to switch it manually

### Connecting Cursive / IntelliJ IDEA to REPL Problems

I can connect to REPL inside the IDE, but the code doesn't seem to be able to send to REPL. But when trying to load any code into REPL I get a `Cannot load Clojure form into ClojureScript REPL`.

My process:

Open IDEA, not yet connected to REPL and not running a terminal with `make run-clojure`.

![](https://notes.status.im/uploads/upload_a2abb27acbee62a0ca9ffdc41d2219dc.png)

Attempting to connect to REPL without `make run-clojure` gives the expected error. After a terminal is running `make run-clojure`, connection works fine.

Entering simple functions gives a response.

![](https://notes.status.im/uploads/upload_9b2848993feb4181ad8fee96fc0dd6e9.png)

However trying to do any thing REPL-ly with the code always gives a "*Cannot load Clojure form into ClojureScript REPL*" error. 

![](https://notes.status.im/uploads/upload_d29b3ded54afdfddf0a2cb09bfdbd121.png)

![](https://notes.status.im/uploads/upload_c07a7805e1e7c954e3d3698063410bb5.png)

---

### Things I've also tried

I've done some Googling to see what could be going wrong.


https://github.com/cursive-ide/cursive/issues/1285 This issue suggests switching the file to `cljc` which I did out of curiosity, it connects better but it doesn't really work.

![](https://notes.status.im/uploads/upload_79bca0edc0c1841a7cbdcec50a4f90eb.png)

Obviously this isn't ideal even if it worked perfectly, I presume that the file types are important and converting them back and forth is a terrible work flow.

I tried converting the files to `clj`, just to see if that helped, which gave an interesting error:

```bash=
Loading src/status_im/reloader.clj... 
Syntax error (FileNotFoundException) compiling at (src/status_im/reloader.clj:1:1).
Could not locate status_im/ui/components/react__init.class, status_im/ui/components/react.clj or status_im/ui/components/react.cljc on classpath. Please check that namespaces with dashes use underscores in the Clojure file name.
```