(ns status-im.chat.handlers.commands
  (:require [re-frame.core :refer [enrich after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.status :as status]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.commands.utils :as cu]
            [status-im.utils.phone-number :as pn]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]
            [status-im.utils.platform :as platform]))

(def command-prefix "c ")

(defn content-by-command
  [{:keys [type]} content]
  (if (and (= :command type) content)
    (subs content (count command-prefix))
    content))

(defn invoke-suggestions-handler!
  [{:keys [current-chat-id canceled-command] :as db} _]
  (when-not canceled-command
    (let [{:keys [command content]} (get-in db [:chats current-chat-id :command-input])
          {:keys [name type]} command
          path [(if (= :command type) :commands :responses)
                name
                :params
                0
                :suggestions]
          params {:value (content-by-command command content)}]
      (status/call-jail current-chat-id
                        path
                        params
                        #(dispatch [:suggestions-handler {:command command
                                                          :content content
                                                          :chat-id current-chat-id} %])))))

(defn cancel-command!
  [{:keys [canceled-command]}]
  (when canceled-command
    (dispatch [:start-cancel-command])))

(defn current-command
  [{:keys [current-chat-id] :as db} k]
  (get-in db [:chats current-chat-id :command-input :command k]))

(register-handler :set-chat-command-content
  [(after (fn [db]
            (let [trigger (keyword (current-command db :suggestions-trigger))]
              (when (= :on-change trigger)
                (invoke-suggestions-handler! db nil)))))
   (after cancel-command!)
   (after #(dispatch [:clear-validation-errors]))]
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (let [starts-as-command? (str/starts-with? content command-prefix)
          command? (= :command (current-command db :type))]
      (as-> db db
            (commands/set-chat-command-content db content)
            (assoc-in db [:chats current-chat-id :input-text] nil)
            (assoc db :canceled-command (and command? (not starts-as-command?)))))))

(defn invoke-command-preview!
  [{:keys [staged-command]} [_ chat-id]]
  (let [{:keys [command content id]} staged-command
        {:keys [name type]} command
        path [(if (= :command type) :commands :responses)
              name
              :preview]
        params {:value content
                :platform platform/platform}]
    (status/call-jail chat-id
                      path
                      params
                      #(dispatch [:command-preview chat-id id %]))))

(defn command-input
  ([{:keys [current-chat-id] :as db}]
   (command-input db current-chat-id))
  ([db chat-id]
   (get-in db [:chats chat-id :command-input])))


(register-handler ::validate!
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id]} {:keys [error result]}]]
      ;; todo handle error
      (when-not error
        (let [{:keys [errors validationHandler parameters]} result]
          (cond errors
                (dispatch [::add-validation-errors chat-id errors])

                validationHandler
                (dispatch [::validation-handler!
                           chat-id
                           validationHandler
                           parameters])

                :else (dispatch [::finish-command-staging chat-id])))))))

(defn start-validate! [db]
  (let [{:keys [content command chat-id] :as data} (::command db)
        {:keys [name type]} command
        path [(if (= :command type) :commands :responses)
              name
              :validator]
        params {:value   content
                :command data}]
    (status/call-jail chat-id
                      path
                      params
                      #(dispatch [::validate! data %]))))

(register-handler :stage-command
  (after start-validate!)
  (fn [{:keys [current-chat-id current-account-id] :as db}]
    (let [{:keys [command content]} (command-input db)
          content' (content-by-command command content)]
      (-> db
          (assoc ::command {:content content'
                            :command command
                            :chat-id current-chat-id
                            :address current-account-id})
          (assoc-in [:disable-staging current-chat-id] true)))))

(register-handler ::finish-command-staging
  [(after #(dispatch [:start-cancel-command]))
   (after invoke-command-preview!)]
  (fn [db [_ chat-id]]
    (let [db (assoc-in db [:chats chat-id :input-text] nil)
          {:keys [command content to-message-id]} (command-input db)
          content' (content-by-command command content)
          command-info {:command    command
                        :content    content'
                        :to-message to-message-id
                        :created-at (time/now-ms)
                        :id         (random/id)}]
      (-> db
          (commands/stage-command command-info)
          (assoc :staged-command command-info)
          (assoc-in [:disable-staging chat-id] true)))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (commands/unstage-command db staged-command)))

(defn set-chat-command
  [{:keys [current-chat-id] :as db} [_ command-key type]]
  (-> db
      (commands/set-command-input (or type :commands) command-key)
      (assoc-in [:chats current-chat-id :command-input :content] command-prefix)
      (assoc :disable-input true)))

(register-handler :set-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:set-soft-input-mode :resize]))
   (after #(dispatch [:command-edit-mode]))]
  set-chat-command)

(defn set-response-command [db [_ to-message-id command-key]]
  (-> db
      (commands/set-command-input :responses to-message-id command-key)
      (assoc :canceled-command false)))

(register-handler :set-response-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))
   (after #(dispatch [:set-chat-input-text ""]))]
  set-response-command)

(register-handler ::add-validation-errors
  (after #(dispatch [:fix-response-height]))
  (fn [db [_ chat-id errors]]
    (assoc-in db [:custom-validation-errors chat-id]
              (map cu/generate-hiccup errors))))

(register-handler :clear-validation-errors
  (fn [db]
    (dissoc db :validation-errors :custom-validation-errors)))

(defn dispatch-error!
  [chat-id title description]
  (letfn [(wrap-params [p] (if (seqable? p) p [p]))]
    (dispatch [::set-validation-error
               chat-id
               {:title       (apply i18n/label (wrap-params title))
                :description (apply i18n/label (wrap-params description))}])))

(def validation-handlers
  {:phone (fn [chat-id [number]]
            (if (pn/valid-mobile-number? number)
              (dispatch [::finish-command-staging chat-id])
              (dispatch-error! chat-id :t/phone-number :t/invalid-phone)))})

(defn validator [name]
  (validation-handlers (keyword name)))

(register-handler ::validation-handler!
  (u/side-effect!
    (fn [_ [_ chat-id name params]]
      (when-let [handler (validator name)]
        (handler chat-id params)))))

(register-handler ::set-validation-error
  (after #(dispatch [:fix-response-height]))
  (fn [db [_ chat-id error]]
    (assoc-in db [:validation-errors chat-id] [error])))

(register-handler :invoke-commands-suggestions!
  (u/side-effect!
    invoke-suggestions-handler!))
