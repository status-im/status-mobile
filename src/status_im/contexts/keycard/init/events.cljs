(ns status-im.contexts.keycard.init.events
  (:require [utils.re-frame :as rf]))

(defn- show-different-card-screen
  []
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:open-modal :screen/keycard.different-card]))

(defn- verify-entered-pin-and-continue
  [on-success instance-uid]
  (fn [pin]
    (rf/dispatch
     [:keycard/connect
      {:theme :dark
       :instance-uid instance-uid
       :on-success
       (fn []
         (rf/dispatch
          [:keycard/verify-pin
           {:pin        pin
            :on-success #(on-success pin)
            :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]))}])))

(defn- init-card-with-pin-and-continue
  [on-success]
  (fn [pin]
    (rf/dispatch
     [:keycard/connect
      {:theme :dark
       :on-success
       (fn [{:keys [initialized?]}]
         (if initialized?
           (show-different-card-screen)
           (rf/dispatch
            [:keycard/init-card
             {:pin        pin
              :on-success #(rf/dispatch
                            [:keycard/get-application-info
                             {:on-success
                              (fn [{:keys [has-master-key?]}]
                                (if has-master-key?
                                  (show-different-card-screen)
                                  (on-success pin)))}])}])))}])))

(rf/reg-event-fx :keycard/init.create-or-enter-pin
 (fn [{:keys [db]} [{:keys [on-success]}]]
   (let [{:keys [initialized? instance-uid]} (get-in db [:keycard :application-info])]
     {:fx [(if initialized?
             [:dispatch
              [:open-modal :screen/keycard.pin.enter
               {:on-complete (verify-entered-pin-and-continue on-success instance-uid)}]]
             [:dispatch
              [:open-modal :screen/keycard.pin.create
               {:on-complete (init-card-with-pin-and-continue on-success)}]])]})))
