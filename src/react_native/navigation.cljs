(ns react-native.navigation
  (:refer-clojure :exclude [pop])
  (:require ["react-native-navigation" :refer (Navigation)]))

(defn set-default-options
  [opts]
  (.setDefaultOptions ^js Navigation (clj->js opts)))

(defn register-component [arg1 arg2 arg3] (.registerComponent ^js Navigation arg1 arg2 arg3))
(defn set-lazy-component-registrator [handler] (.setLazyComponentRegistrator ^js Navigation handler))

(defn set-root
  [root]
  (.setRoot ^js Navigation (clj->js root)))

(defn set-stack-root
  [stack component]
  (.setStackRoot ^js Navigation stack (clj->js component)))

(defn push
  [arg1 arg2]
  (.push ^js Navigation arg1 (clj->js arg2)))

(defn pop
  [component]
  (.pop ^js Navigation component))

(defn show-modal
  [component]
  (.showModal ^js Navigation (clj->js component)))

(defn dismiss-modal
  [component]
  (.dismissModal ^js Navigation component))

(defn show-overlay
  [component]
  (.showOverlay Navigation (clj->js component)))

(defn pop-to
  [component]
  (.popTo Navigation (clj->js component)))

(defn pop-to-root
  [tab]
  (.popToRoot Navigation (clj->js tab)))

(defn dissmiss-overlay
  [component]
  (.catch (.dismissOverlay Navigation component) #()))

(defn reg-app-launched-listener
  [handler]
  (.registerAppLaunchedListener ^js (.events ^js Navigation) handler))

(defn reg-button-pressed-listener
  [handler]
  (.registerNavigationButtonPressedListener
   (.events Navigation)
   (fn [^js evn]
     (handler (.-buttonId evn)))))

(defn reg-modal-dismissed-listener
  [handler]
  (.registerModalDismissedListener ^js (.events ^js Navigation) handler))

(defn reg-component-did-appear-listener
  [handler]
  (.registerComponentDidAppearListener
   ^js (.events ^js Navigation)
   (fn [^js evn]
     (handler (keyword (.-componentName evn))))))

(defn reg-component-did-disappear-listener
  [handler]
  (.registerComponentDidDisappearListener
   ^js (.events ^js Navigation)
   (fn [^js evn]
     (handler (.-componentName evn)))))

(defn merge-options
  [id opts]
  (.mergeOptions Navigation id (clj->js opts)))

(def constants (atom nil))

(defn status-bar-height
  []
  (:status-bar-height @constants))

(.then (.constants Navigation)
       (fn [^js consts]
         (reset! constants {:top-bar-height     (.-topBarHeight consts)
                            :bottom-tabs-height (.-bottomTabsHeight consts)
                            :status-bar-height  (.-statusBarHeight consts)})))

(defn bind-component
  [^js/Object this component-id]
  (set! (. this -navigationEventListener)
    (.. Navigation events (bindComponent this component-id))))
