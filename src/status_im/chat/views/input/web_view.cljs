(ns status-im.chat.views.input.web-view
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch]]
            [status-im.components.webview-bridge :refer [webview-bridge]]
            [status-im.components.react :refer [view
                                                text]]
            [status-im.i18n :refer [label]]
            [status-im.utils.js-resources :as js-res]
            [clojure.string :as str]))

(defn on-navigation-change
  [event {:keys [dynamicTitle] :as result-box}]
  (let [{:strs [loading url title]} (js->clj event)]
    (when-not (= "about:blank" url)
      (when-not loading
        (dispatch [:set-command-argument [0 url]]))
      (when (and dynamicTitle (not (str/blank? title)))
        (dispatch [:set-chat-ui-props :result-box (assoc result-box :title title)])))))

(defn web-view-error []
  (r/as-element
    [view {:justify-content :center
           :align-items     :center
           :flex-direction  :row}
     [text (label :t/web-view-error)]]))

(defview bridged-web-view [{:keys [url]}]
  [extra-js [:web-view-extra-js]
   rpc-url [:get :rpc-url]
   result-box [:chat-ui-props :result-box]]
  (when url
    [webview-bridge
     {:ref                                   #(dispatch [:set-webview-bridge %])
      :on-bridge-message                     #(dispatch [:webview-bridge-message %])
      :source                                {:uri url}
      :render-error                          web-view-error
      :java-script-enabled                   true
      :injected-on-start-loading-java-script (str js-res/web3
                                                  js-res/jquery
                                                  (js-res/web3-init rpc-url))
      :injected-java-script                  (str js-res/webview-js extra-js)
      :bounces                               false
      :on-navigation-state-change            #(on-navigation-change % result-box)
      :local-storage-enabled                 true}]))