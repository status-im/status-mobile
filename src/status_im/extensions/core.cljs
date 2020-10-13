(ns status-im.extensions.core
  (:require ["extension" :default test-extension]
            [status-im.utils.fx :as fx]
            [cljs-bean.core :as bean]
            [re-frame.core :as re-frame]))

(def extensions [{:id          "test.todo-extension"
                  :name        "TODO"
                  :author      "andrey.stateofus.eth"
                  :version     "1.0"
                  :description "Test extension test test"
                  :hooks       (bean/->clj test-extension)}])

(defn send-command [ext]
  (fn [params]
    (re-frame/dispatch [:send-extension-command {:id     (:id ext)
                                                 :params (.stringify js/JSON ^js params)}])))

(re-frame/reg-fx
 ::init-extension
 (fn [ext]
   (doseq [hook (:hooks ext)]
     ((:init hook) #js {:sendCommand (send-command ext)
                        :close #(re-frame/dispatch [:bottom-sheet/hide])}))))

(fx/defn join-time-messages-checked-for-chats
  {:events [:switch-extension]}
  [{:keys [db]} {:keys [id] :as ext}]
  (merge
   {:db (update-in db [:extensions id] #(when-not % ext))}
   (when-not (get-in db [:extensions id])
     {::init-extension ext})))