(ns ^:figwheel-always status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(def commands-js (slurp "resources/commands.js"))
(def console-js (slurp "resources/console.js"))
(def status-js (slurp "resources/status.js"))
(def wallet-js (str commands-js (slurp "resources/wallet.js")))
(def webview-js (slurp "resources/webview.js"))
