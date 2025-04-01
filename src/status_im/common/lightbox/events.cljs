(ns status-im.common.lightbox.events
  (:require [reagent.core :as reagent]
            status-im.common.lightbox.effects
            [utils.re-frame :as rf]))

(rf/reg-event-fx :lightbox/navigate-to-lightbox
 (fn [{:keys [db]} [animation-shared-element-id screen-params]]
   (reagent/next-tick #(rf/dispatch [:navigate-to :screen/lightbox screen-params]))
   {:db (assoc db :animation-shared-element-id animation-shared-element-id)}))

(rf/reg-event-fx :lightbox/update-animation-shared-element-id
 (fn [{:keys [db]} [animation-shared-element-id]]
   {:db (assoc db :animation-shared-element-id animation-shared-element-id)}))

(rf/reg-event-fx :lightbox/exit-lightbox-signal
 (fn [{:keys [db]} [value]]
   {:db (assoc db :lightbox/exit-signal value)}))

(rf/reg-event-fx :lightbox/zoom-out-signal
 (fn [{:keys [db]} [value]]
   {:db (assoc db :lightbox/zoom-out-signal value)}))

(rf/reg-event-fx :lightbox/orientation-change
 (fn [{:keys [db]} [value]]
   {:db (assoc db :lightbox/orientation value)}))

(rf/reg-event-fx :lightbox/lightbox-scale
 (fn [{:keys [db]} [value]]
   {:db (assoc db :lightbox/scale value)}))

(rf/reg-event-fx :lightbox/share-image
 (fn [_ [uri]]
   {:effects.lightbox/share-image uri}))

(rf/reg-event-fx :lightbox/save-image-to-gallery
 (fn [_ [uri on-success]]
   {:effects.lightbox/save-image-to-gallery [uri on-success]}))
