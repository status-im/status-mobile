(ns status-im.navigation2
  (:require [status-im.utils.fx :as fx]
            [status-im.reloader :as reloader]
            [status-im.utils.datetime :as datetime]))

(def parent-stack (atom :shell-stack))

(fx/defn reload-new-ui
  {:events [:reload-new-ui]}
  [_]
  (reloader/reload)
  {:new-ui/reset-bottom-tabs nil
   :dispatch                 [:init-root :shell-stack]})

(fx/defn init-root-nav2
  {:events [:init-root-nav2]}
  [_ root-id]
  {:init-root-fx-nav2 root-id})

(fx/defn open-modal-nav2
  {:events [:open-modal-nav2]}
  [_ modal]
  {:open-modal-fx-nav2 modal})

(fx/defn close-modal-nav2
  {:events [:close-modal-nav2]}
  [_ modal]
  {:close-modal-fx-nav2 modal})

(defn navigate-from-shell-stack [go-to-view-id id db]
  (reset! parent-stack go-to-view-id)
  {:navigate-to-fx-nav2 [go-to-view-id id]
   :db (assoc-in db [:navigation2/navigation2-stacks id] {:type  go-to-view-id
                                                          :id    id
                                                          :clock (datetime/timestamp)})})

(defn navigate-from-switcher [go-to-view-id id db from-home?]
  (reset! parent-stack go-to-view-id)
  {:navigate-from-switcher-fx [go-to-view-id id from-home?]
   :db (assoc-in db [:navigation2/navigation2-stacks id] {:type  go-to-view-id
                                                          :id    id
                                                          :clock (datetime/timestamp)})})

(fx/defn navigate-to-nav2
  {:events [:navigate-to-nav2]}
  [{:keys [db]} go-to-view-id id _ from-switcher?]
  (let [view-id     (:view-id db)
        from-home?  (= view-id :chat-stack)]
    (if from-switcher?
      (navigate-from-switcher go-to-view-id id db from-home?)
      (if from-home?
        (navigate-from-shell-stack go-to-view-id id db)
        ;; TODO(parvesh) - new stacks created from other screens should be stacked on current stack, instead of creating new entry
        (navigate-from-shell-stack go-to-view-id id db)))))

(fx/defn change-root-status-bar-style
  {:events [:change-root-status-bar-style]}
  [_ style]
  {:change-root-status-bar-style-fx style})

