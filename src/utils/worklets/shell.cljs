(ns utils.worklets.shell)

(def bottom-tabs-worklets (js/require "../src/js/worklets/shell/bottom_tabs.js"))
(def home-stack-worklets (js/require "../src/js/worklets/shell/home_stack.js"))

;; Derived values for Bottom tabs
(defn bottom-tab-icon-color
  [id selected-stack-id selected-tab-color default-color]
  (.bottomTabIconColor ^js bottom-tabs-worklets id selected-stack-id selected-tab-color default-color))

;; Derived values for stacks (communities, chat, wallet, browser)
(defn stack-opacity
  [id selected-stack-id]
  (.stackOpacity ^js home-stack-worklets id selected-stack-id))

(defn stack-z-index
  [id selected-stack-id]
  (.stackZIndex ^js home-stack-worklets id selected-stack-id))
