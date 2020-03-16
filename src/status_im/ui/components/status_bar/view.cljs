(ns status-im.ui.components.status-bar.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.styles :as styles]
            [status-im.utils.platform :as platform]))

(def route->bar-type (merge {:qr-scanner {:type :black}}
                            (when platform/ios?
                              {:new-chat        {:type :black}
                               :new-public-chat {:type :black}})))

;; TODO: Integrate into navigation
(defn get-config [view-id]
  (get route->bar-type view-id {:type :main}))

(defn set-status-bar
  "If more than one `StatusBar` is rendered, the one which was mounted last will
  have higher priority
  https://facebook.github.io/react-native/docs/statusbar.html

  This means that if we have more than one screen rendered at the same time
  (which happens due to bottom nav change), it might happen that the screen
  which was already rendered before will be reopened but the `StatusBar` from
  a different screen which still exists but is blurred will be applied.
  Thus we need to set props to `StatusBar` imperatively when a particular screen
  is shown. At the same time, the background of `StatusBar` should be rendered
  inside the screen in order to have better transitions between screens."
  [view-id]
  (let [{:keys [type]} (get-config view-id)
        {:keys [background-color bar-style hidden
                network-activity-indicator-visible
                translucent]}
        (case type
          :black (styles/status-bar-black)
          (styles/status-bar-default))]
    (when bar-style
      (.setBarStyle react/status-bar-class (clj->js bar-style)) true)
    (when (and background-color platform/android?)
      (.setBackgroundColor react/status-bar-class (clj->js background-color)))
    (when hidden
      (.setHidden react/status-bar-class (clj->js hidden)))
    (when network-activity-indicator-visible
      (.setNetworkActivityIndicatorVisible
       react/status-bar-class
       (clj->js network-activity-indicator-visible)))
    (when translucent
      (.setTranslucent react/status-bar-class (clj->js translucent)))))
