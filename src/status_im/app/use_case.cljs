(ns status-im.app.use-case)

(defmulti initial-state
  (fn [use-case-id]
    use-case-id))

(defmethod initial-state :default
  [_]
  {:use-case-id :default})

(defn start
  [use-case-id state-update]
  (merge (initial-state use-case-id)
         state-update))

(defn make-step
  [current-state state-update]
  (merge current-state state-update))

(defn finish
  [use-case-id state-update]
  (merge (initial-state use-case-id)
         state-update))


;; -----------------------------------------------------

(defmethod initial-state :uc-view-saved-addresses
  [_]
  {:use-case-id :uc-view-saved-addresses})

(defmethod initial-state :uc-add-saved-address
  [_]
  {:use-case-id         :uc-add-saved-address
   :address             nil
   :ens                 nil
   :ens?                false
   :name                nil
   :customization-color nil})

;; -----------------------------------------------------

(defmulti navigation-start
  (fn [use-case-data]
    (:use-case-id use-case-data)))

(defmulti navigation-step
  (fn [use-case-data]
    (:use-case-id use-case-data)))

(defmethod navigation-step :default
  []
  nil)

(defmulti navigation-finish
  (fn [use-case-data]
    (:use-case-id use-case-data)))

;; -----------------------------------------------------

(defmethod navigation-start :uc-view-saved-addresses
  [_]
  [[:dispatch [:open-modal :screen/settings.saved-addresses]]])

(defmethod navigation-finish :uc-view-saved-addresses
  [_]
  [[:dispatch [:navigate-back]]])

;; -----------------------------------------------------

(defmethod navigation-start :uc-add-saved-address
  [_]
  [[:dispatch [:open-modal :screen/settings.add-address-to-save]]])

(defmethod navigation-step :uc-add-saved-address
  [{:keys [address ens ens?] :as use-case-state}]
  [[:dispatch [:open-modal :screen/settings.add-address-to-save]]])

(defmethod navigation-finish :uc-add-saved-address
  [_]
  [[:dispatch [:navigate-back]]])

;; -----------------------------------------------------
(comment
  (initial-state :uc-add-saved-address)
  (initial-state :uc-view-saved-addresses)
  (initial-state :unknown)

  (navigation-start {:use-case-id :uc-add-saved-address})
  (navigation-finish {:use-case-id :uc-add-saved-address})
  (navigation-start {:use-case-id :uc-add-saved-address})

  (-> :uc-add-saved-address
      initial-state
      (make-step {:address "0xasdfas"})))


