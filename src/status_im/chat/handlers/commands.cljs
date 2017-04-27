(ns status-im.chat.handlers.commands
  (:require [re-frame.core :refer [enrich after dispatch]]
            [status-im.utils.handlers :as handlers]
            [status-im.components.status :as status]
            [status-im.chat.constants :as const]
            [status-im.commands.utils :as cu]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(handlers/register-handler :request-command-data
  (handlers/side-effect!
    (fn [{:keys [contacts current-account-id] :as db}
         [_ {{:keys [command params content-command type]} :content
             :keys [message-id chat-id on-requested jail-id] :as message} data-type]]
      (let [jail-id (or jail-id chat-id)]
        (if-not (get-in contacts [jail-id :commands-loaded])
          (do (dispatch [:add-commands-loading-callback
                         jail-id
                         #(dispatch [:request-command-data message data-type])])
              (dispatch [:load-commands! jail-id]))
          (let [path     [(if (= :response (keyword type)) :responses :commands)
                          (if content-command content-command command)
                          data-type]
                to       (get-in contacts [chat-id :address])
                params   {:parameters params
                          :context    (merge {:platform platform/platform
                                              :from     current-account-id
                                              :to       to}
                                             i18n/delimeters)}
                callback #(let [result (get-in % [:result :returned])
                                result (if (:markup result)
                                         (update result :markup cu/generate-hiccup)
                                         result)]
                            (dispatch [:set-in [:message-data data-type message-id] result])
                            (when on-requested (on-requested result)))]
            (status/call-jail jail-id path params callback)))))))
