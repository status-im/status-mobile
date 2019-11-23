(ns status-im.ui.components.status-bar.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.styles :as styles]
            [status-im.utils.platform :as platform]))

(defn get-config [view-id]
  (or (get {:recipient-qr-code {:type :transparent}}
           view-id)
      {:type :main}))

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
          :transparent styles/status-bar-transparent
          styles/status-bar-default)]
    (when (and background-color platform/android?)
      (.setBackgroundColor react/status-bar-class (clj->js background-color)))
    (when bar-style
      (.setBarStyle react/status-bar-class (clj->js bar-style)))
    (when hidden
      (.setHidden react/status-bar-class (clj->js hidden)))
    (when network-activity-indicator-visible
      (.setNetworkActivityIndicatorVisible
       react/status-bar-class
       (clj->js network-activity-indicator-visible)))
    (when translucent
      (.setTranslucent react/status-bar-class (clj->js translucent)))))
