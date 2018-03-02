(ns status-im.ui.components.theme
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.utils.platform :as platform]))

(defn- theme-styles [type]
  {:main-background-color   (case type
                              (:wallet :wallet-home
                               :wallet-2 :modal-wallet) colors/blue
                              :chat                     colors/gray-lighter
                              :qr-code                  colors/white-light-transparent
                              colors/white)
   :device-background-color (case type
                              (:wallet :wallet-home
                               :wallet-2 :modal-wallet) colors/blue
                              (:modal :qr-code)         colors/gray-dark
                              colors/white)
   :status-bar-type         (case type
                              (:wallet :wallet-2)  :wallet
                              :wallet-home         :wallet-tab
                              :modal-wallet        :modal-wallet
                              :modal-white         :modal-white
                              :modal               :modal
                              :qr-cod              :transparent
                              :transactions        nil
                              :main)
   :status-bar-flat?        (if (#{:accounts :intro} type)
                              true
                              false)})

(defn theme [type avoid-keyboard? body]
  (let [{:keys [main-background-color device-background-color
                status-bar-type status-bar-flat?]} (theme-styles type)
        main-view (if avoid-keyboard? react/keyboard-avoiding-view react/view)]
    [react/platform-specific-view {:flex             1
                                   :background-color device-background-color}
     [status-bar/status-bar {:type status-bar-type
                             :flat? status-bar-flat?}]
     [main-view {:flex             1
                 :background-color main-background-color}
      body]
     (when (and platform/iphone-x? (#{:wallet-home :wallet-2} type))
       [react/view (styles/iphone-x-bottom-bar-color-fill colors/white)])]))
