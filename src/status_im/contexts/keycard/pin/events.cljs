(ns status-im.contexts.keycard.pin.events
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard.pin/delete-pressed
 (fn [{:keys [db]}]
   (let [pin (get-in db [:keycard :pin :text])]
     (when (and pin (pos? (count pin)))
       {:db (-> db
                (assoc-in [:keycard :pin :text] (.slice pin 0 -1))
                (assoc-in [:keycard :pin :status] nil))}))))

(rf/reg-event-fx :keycard.pin/number-pressed
 (fn [{:keys [db]} [number max-numbers on-complete-event]]
   (let [pin     (get-in db [:keycard :pin :text])
         new-pin (str pin number)]
     (when (<= (count new-pin) max-numbers)
       {:db (-> db
                (assoc-in [:keycard :pin :text] new-pin)
                (assoc-in [:keycard :pin :status] nil))
        :fx [(when (= (dec max-numbers) (count pin))
               [:dispatch [on-complete-event]])]}))))
