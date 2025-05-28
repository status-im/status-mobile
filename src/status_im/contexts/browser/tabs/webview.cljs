(ns status-im.contexts.browser.tabs.webview
  (:require [react-native.webview :as webview]
            [status-im.contexts.browser.js-scripts.core :as js-scripts]
            [utils.re-frame :as rf]))

(defn make-injected-script
  [tab-id]
  (->
    ""
    (js-scripts/add-global-var "WALLET_UUID" tab-id)
    (js-scripts/add-global-var "WALLET_NAME" "Status")
    (js-scripts/add-global-var
     "WALLET_ICON"
     "https://play-lh.googleusercontent.com/VZTM4ybMq2LtICfiF_nYOlId_TfgVo1rgACYrPSUqcuY4MplG-CvWw-1dN4XPKTNixmc=w240-h480-rw")
    (js-scripts/add-global-var "WALLET_RDNS" "im.status.ethereum")
    (js-scripts/add-script js-scripts/website-metadata)
    (js-scripts/add-script js-scripts/web3-provider)))

(defn view
  [{:keys [tab-id url]}]
  [webview/view
   {:ref #(rf/dispatch [:browser/set-tab-ref tab-id %])
    :container-style {:border-radius 20}
    :source {:uri url}
    :java-script-enabled true
    :bounces false
    :cache-enabled true
    :local-storage-enabled true
    :set-support-multiple-windows false
    ;;:injected-java-script js-scripts/website-metadata
    :injected-java-script-before-content-loaded (make-injected-script tab-id)
    :on-message #(rf/dispatch [:browser/on-message tab-id %])
    :on-error #(println :on-error %)
    :allows-back-forward-navigation-gestures true ;; iOS only
    :pull-to-refresh-enabled true
    :webview-debugging-enabled true
    ;; https://github.com/status-im/status-mobile/issues/17854
    :allows-inline-media-playback true
    :mixed-content-mode :always
    :origin-white-list ["*"]
    :user-agent
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Mobile Safari/537.36"
    ;;:render-error                               web-view-error
    ;;:on-navigation-state-change                 #()
    ;; :on-message                                #(re-frame/dispatch
    ;; [:browser/bridge-message-received
    ;;                                                                  (.. ^js % -nativeEvent
    ;;                                                                  -data)])
    ;; :on-load                                    #(re-frame/dispatch [:browser/loading-started])
    ;; :on-error                                   #(re-frame/dispatch [:browser/error-occured])
    ;; :injected-java-script-before-content-loaded (js-res/ethereum-provider (str network-id))
    ;; https://github.com/status-im/status-mobile/issues/17854
   }])
(
)
