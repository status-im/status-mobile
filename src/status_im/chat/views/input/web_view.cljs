(ns status-im.chat.views.input.web-view
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch]]
            [status-im.components.webview-bridge :refer [webview-bridge]]
            [status-im.components.react :refer [view
                                                text]]
            [status-im.components.status :as status]
            [status-im.i18n :refer [label]]
            [status-im.utils.js-resources :as js-res]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn on-navigation-change
  [event {:keys [dynamicTitle actions] :as result-box}]
  (let [{:strs [loading url title canGoBack canGoForward]} (js->clj event)]
    (when-not (= "about:blank" url)
      (when-not loading
        (dispatch [:set-command-argument [0 url]]))
      (let [result-box (assoc result-box :can-go-back? canGoBack :can-go-forward? canGoForward)
            result-box (if (and dynamicTitle (not (str/blank? title)))
                         (assoc result-box :title title)
                         result-box)]
        (dispatch [:set-chat-ui-props {:result-box result-box}])))))

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