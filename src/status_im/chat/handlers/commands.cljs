(ns status-im.chat.handlers.commands
  (:require [re-frame.core :refer [enrich after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.jail :as j]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.commands.utils :as cu]
            [status-im.utils.phone-number :as pn]
            [status-im.i18n :as i18n]))

(def command-prefix "c ")

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
          params {:value content}]
      (j/call current-chat-id
              path
              params
              #(dispatch [:suggestions-handler {:command command
                                                :content content
                                                :chat-id current-chat-id} %])))))

(defn cancel-command!
  [{:keys [canceled-command]}]
  (when canceled-command
    (dispatch [:start-cancel-command])))

(register-handler :set-chat-command-content
  [(after invoke-suggestions-handler!)
   (after cancel-command!)
   (after #(dispatch [:clear-validation-errors]))]
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (let [starts-as-command? (str/starts-with? content command-prefix)
          path [:chats current-chat-id :command-input :command :type]
          command? (= :command (get-in db path))]
      (as-> db db
            (commands/set-chat-command-content db content)
            (assoc-in db [:chats current-chat-id :input-text] nil)
            (assoc db :canceled-command (and command? (not starts-as-command?)))))))

(defn invoke-command-preview!
  [{:keys [staged-command]} [_ chat-id]]
  (let [{:keys [command content]} staged-command
        {:keys [name type]} command
        path [(if (= :command type) :commands :responses)
              name
              :preview]
        params {:value content}]
    (j/call chat-id
            path
            params
            #(dispatch [:command-preview chat-id %]))))

(defn content-by-command
  [{:keys [type]} content]
  (if (= :command type)
    (subs content 2)
    content))

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
        params {:value content}]
    (j/call chat-id
            path
            params
            #(dispatch [::validate! data %]))))

(register-handler :stage-command
  (after start-validate!)
  (fn [{:keys [current-chat-id] :as db}]
    (let [{:keys [command content]} (command-input db)
          content' (content-by-command command content)]
      (-> db
          (assoc ::command {:content content'
                            :command command
                            :chat-id current-chat-id})
          (assoc-in [:disable-staging current-chat-id] true)))))

(register-handler ::finish-command-staging
  [(after #(dispatch [:start-cancel-command]))
   (after invoke-command-preview!)]
  (fn [db [_ chat-id]]
    (let [db (assoc-in db [:chats chat-id :input-text] nil)
          {:keys [command content to-msg-id]} (command-input db)
          content' (content-by-command command content)
          command-info {:command    command
                        :content    content'
                        :to-message to-msg-id}]
      (-> db
          (commands/stage-command command-info)
          (assoc :staged-command command-info)
          (assoc-in [:disable-staging chat-id] true)))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (commands/unstage-command db staged-command)))

(defn set-chat-command
  [{:keys [current-chat-id] :as db} [_ command-key]]
  (-> db
      (commands/set-chat-command command-key)
      (assoc-in [:chats current-chat-id :command-input :content] command-prefix)
      (assoc :disable-input true)))

(register-handler :set-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))]
  set-chat-command)

(defn set-response-command [db [_ to-msg-id command-key]]
  (-> db
      (commands/set-response-chat-command to-msg-id command-key)
      (assoc :canceled-command false)))

(register-handler :set-response-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))
   (after #(dispatch [:set-chat-input-text ""]))]
  set-response-command)

(register-handler ::add-validation-errors
  (fn [db [_ chat-id errors]]
    (assoc-in db [:custom-validation-errors chat-id]
              (map cu/generate-hiccup errors))))

(register-handler :clear-validation-errors
  (fn [db]
    (dissoc db :validation-errors :custom-validation-errors)))

(def validation-handlers
  {:phone (fn [chat-id [number]]
            (if (pn/valid-mobile-number? number)
              (dispatch [::finish-command-staging chat-id])
              (dispatch [::set-validation-error
                         chat-id
                         {:title       (i18n/label :t/phone-number)
                          :description (i18n/label :t/invalid-phone)}])))})

(defn validator [name]
  (validation-handlers (keyword name)))

(register-handler ::validation-handler!
  (u/side-effect!
    (fn [_ [_ chat-id name params]]
      (when-let [handler (validator name)]
        (handler chat-id params)))))


(register-handler ::set-validation-error
  (fn [db [_ chat-id error]]
    (assoc-in db [:validation-errors chat-id] [error])))
