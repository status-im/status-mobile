(ns status-im.contexts.browser.events.screenshots
  (:require [re-frame.core :as rf]
            [react-native.core :as rn]
            [react-native.view-shot :as view-shot]
            [taoensso.timbre :as log]))

(rf/reg-event-fx :browser.screenshots/capture
 (fn [{:keys [db]} [tab-id]]
   (let [ref (get-in db [:browser/screenshots tab-id :ref])]
     (when ref
       {:fx [[:fx.promise
              {:promise    #(view-shot/capture ref)
               :on-success [:browser.screenshots/add-url tab-id]
               :on-error   #(log/error "Failed to capture tab screenshot" {:error %})}]]}))))

(rf/reg-event-fx :browser.screenshots/add-url
 (fn [{:keys [db]} [tab-id url]]
   {:db (assoc-in db [:browser/screenshots tab-id :url] url)
    :fx [#_[:dispatch
            [:show-bottom-sheet
             {:content (fn [] [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
                               [rn/image
                                {:source {:uri url}
                                 :style  {:height       300
                                          :width        300
                                          :border-width 2
                                          :border-color :red
                                          :aspect-ratio 0.5
                                          :transform    [{:scale 1}]}}]])}]]]}))

(rf/reg-event-fx :browser.screenshots/add-ref
 (fn [{:keys [db]} [tab-id ref]]
   {:db (assoc-in db [:browser/screenshots tab-id :ref] ref)}))
