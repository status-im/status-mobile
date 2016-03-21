(ns messenger.services.navigate-to
  (:require [syng-im.utils.logging :as log]
            [messenger.state :as state]
            [messenger.components.chat.chat :as chat]
            [messenger.models.chat :refer [set-current-chat-id]]
            [om.next :as om]
            [messenger.omnext :as omnext]
            [messenger.components.iname :as in]))

(defn nav-push [nav route]
  (binding [state/*nav-render* false]
    (.push nav (clj->js route))))

(defn set-root-query [component]
  (let [app-root (om/class->any omnext/reconciler (om/app-root omnext/reconciler))]
    (om/set-query! app-root {:query [{(in/get-name component) (om/get-query component)}]})))

(defmulti navigate-to (fn [state id args]
                        id))

(defmethod navigate-to :scene/chat
  [state id {:keys [navigator chat-id] :as args}]
  (log/debug "handling " id "args = " (dissoc args :navigator))
  (set-current-chat-id chat-id)
  (set-root-query chat/Chat)
  (nav-push navigator {:component chat/chat}))

(defn navigate-to-handler [state [id args]]
  (log/debug "navigate-to-handler: " (dissoc args :navigator))
  (navigate-to state id args))


(comment

  )