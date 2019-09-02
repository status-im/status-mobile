(ns status-im.ui.components.status-bar.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.styles :as styles]
            [status-im.utils.platform :as platform]))

(defn status-bar [{:keys [type flat?]}]
  (let [view-style
        (case type
          :main styles/view-main
          :modal-main styles/view-modal-main
          :transparent styles/view-transparent
          :modal styles/view-modal
          :modal-white styles/view-modal-white
          :modal-wallet styles/view-modal-wallet
          :transaction styles/view-transaction
          :wallet styles/view-wallet
          :wallet-tab styles/view-wallet-tab
          styles/view-default)]
    (when-not platform/desktop?
      [react/view {:style (cond-> view-style flat? (assoc :elevation 0))}])))

(defn get-config [view-id]
  (or (get {:create-multiaccount             {:flat? true}
            :chat-modal                      {:type :modal-white}
            :intro                           {:flat? true}
            :recipient-qr-code               {:type :transparent}
            :wallet-transactions-filter      {:type :modal-main}}
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
          :main styles/status-bar-main
          :modal-main styles/status-bar-main-main
          :transparent styles/status-bar-transparent
          :modal styles/status-bar-modal
          :modal-white styles/status-bar-modal-white
          :modal-wallet styles/status-bar-modal-wallet
          :transaction styles/status-bar-transaction
          :wallet styles/status-bar-wallet
          :wallet-tab styles/status-bar-wallet-tab
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
