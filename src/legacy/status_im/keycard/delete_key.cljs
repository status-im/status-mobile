(ns legacy.status-im.keycard.delete-key
  (:require
    [legacy.status-im.keycard.common :as common]
    [status-im2.navigation.events :as navigation]
    [utils.re-frame :as rf]))

(rf/defn reset-card-pressed
  {:events [:keycard-settings.ui/reset-card-pressed]}
  [cofx]
  (navigation/navigate-to cofx :reset-card nil))

(rf/defn delete-card
  [{:keys [db] :as cofx}]
  (let [key-uid              (get-in db [:keycard :application-info :key-uid])
        multiaccount-key-uid (get-in db [:profile/profile :key-uid])]
    (if (and key-uid
             (= key-uid multiaccount-key-uid))
      {:keycard/delete nil}
      (common/unauthorized-operation cofx))))

(rf/defn navigate-to-reset-card-screen
  {:events [:keycard/navigate-to-reset-card-screen]}
  [cofx]
  (navigation/navigate-to cofx :reset-card nil))

(rf/defn reset-card-next-button-pressed
  {:events [:keycard-settings.ui/reset-card-next-button-pressed]}
  [{:keys [db]}]
  {:db       (assoc-in db [:keycard :reset-card :disabled?] true)
   :dispatch [:keycard/proceed-to-reset-card false]})

(rf/defn proceed-to-reset-card
  {:events [:keycard/proceed-to-reset-card]}
  [{:keys [db] :as cofx} keep-keys-on-keycard?]
  (rf/merge cofx
            {:db (assoc-in db
                  [:keycard :pin]
                  {:enter-step  :current
                   :current     []
                   :puk         []
                   :status      nil
                   :error-label nil
                   :on-verified (if keep-keys-on-keycard?
                                  :keycard/unpair-and-delete
                                  :keycard/remove-key-with-unpair)})}
            (common/set-on-card-connected :keycard/navigate-to-enter-pin-screen)
            (common/navigate-to-enter-pin-screen)))
