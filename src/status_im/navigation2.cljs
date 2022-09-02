(ns status-im.navigation2
  (:require [status-im.utils.fx :as fx]
            [status-im.reloader :as reloader]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.async-storage.core :as async-storage]))

(def parent-stack (atom :home-stack))

(fx/defn toggle-new-ui
  {:events [:toggle-new-ui]}
  [_]
  (swap! config/new-ui-enabled? not)
  (reloader/reload)
  {:new-ui/reset-bottom-tabs nil
   :dispatch                 [:init-root (if @config/new-ui-enabled? :home-stack :chat-stack)]
   ::async-storage/set!      {:new-ui-enabled? @config/new-ui-enabled?}})

(fx/defn toggle-local-pairing-experimental-mode
  {:events [:toggle-local-pairing-experimental-mode]}
  [_]
  (swap! config/local-pairing-mode-enabled? not)
  (reloader/reload)
  {::async-storage/set!      {:local-pairing-mode-enabled? @config/local-pairing-mode-enabled?}})

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

(defn navigate-from-home-stack [go-to-view-id id db]
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
  [{:keys [db] :as cofx} go-to-view-id id screen-params from-switcher?]
  (let [view-id     (:view-id db)
        stacks      (:navigation2/navigation2-stacks db)
        from-home?  (= view-id :chat-stack)]
    (if from-switcher?
      (navigate-from-switcher go-to-view-id id db from-home?)
      (if from-home?
        (navigate-from-home-stack go-to-view-id id db)
        ;; TODO(parvesh) - new stacks created from other screens should be stacked on current stack, instead of creating new entry
        (navigate-from-home-stack go-to-view-id id db)))))


