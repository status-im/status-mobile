(ns status-im.chat.handlers.faucet
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.utils.utils :refer [http-get]]
            [status-im.utils.random :as random]
            [status-im.constants :refer [console-chat-id
                                         text-content-type]]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]
            [goog.string :as gstring]
            goog.string.format))

(def faucets
  [{:name    "http://faucet.ropsten.be:3001"
    :type    :api
    :api-url "http://faucet.ropsten.be:3001/donate/0x%s"}
   {:name    "http://46.101.129.137:3001"
    :type    :api
    :api-url "http://46.101.129.137:3001/donate/0x%s"}
   {:name       "https://zerogox.com/ethereum/wei_faucet"
    :type       :prefill
    :prefill-js "document.getElementById('edit-ethereum-address').value='%s';"}])

(defn faucet-by-name [faucet-name]
  (->> faucets
       (filter #(= (:name %) faucet-name))
       (first)))

(defn received-message [content]
  (dispatch [:received-message
             {:message-id   (random/id)
              :content      content
              :content-type text-content-type
              :outgoing     false
              :chat-id      console-chat-id
              :from         console-chat-id
              :to           "me"}]))

(defmulti open-faucet (fn [_ _ {:keys [type]}] type))

(defmethod open-faucet :api
  [faucet-name current-address {:keys [api-url]}]
  (let [api-url (gstring/format api-url current-address)]
    (http-get api-url
              #(received-message (label :t/faucet-success))
              #(received-message (label :t/faucet-error)))))

(defmethod open-faucet :prefill
  [faucet-name current-address {:keys [prefill-js]}]
  (let [prefill-js  (gstring/format prefill-js current-address)
        web-view-js (gstring/format
                     "document.addEventListener('DOMContentLoaded', function(){ %s };"
                     prefill-js)]
    (dispatch [:set-chat-command :browse])
    (dispatch [:fill-chat-command-content faucet-name])
    (dispatch [:set-web-view-extra-js web-view-js])
    (js/setTimeout #(dispatch [:send-command!]) 500)))

(register-handler
 :open-faucet
 (u/side-effect!
  (fn [{:keys [accounts current-account-id]} [_ faucet-name _]]
    (if-let [faucet (faucet-by-name faucet-name)]
      (let [current-address (get-in accounts [current-account-id :address])]
        (open-faucet faucet-name current-address faucet))))))
