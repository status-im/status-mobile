(ns status-im.ui.screens.extensions.add.events
  (:require [re-frame.core :as re-frame]
            [pluto.registry :as registry]
            [status-im.extensions.registry :as extensions]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(re-frame/reg-fx
 :extension/load
 (fn [[url follow-up-event]]
   (extensions/load-from url #(re-frame/dispatch [follow-up-event (-> % extensions/read-extension extensions/parse)]))))

(handlers/register-handler-fx
 :extension/install
 [re-frame/trim-v]
 (fn [cofx [extension-data]]
   (let [extension-key (get-in extension-data ['meta :name])]
     (handlers-macro/merge-fx cofx
                              {:show-confirmation {:title     (i18n/label :t/success)
                                                   :content   (i18n/label :t/extension-installed)
                                                   :on-accept #(re-frame/dispatch [:navigate-to-clean :home])
                                                   :on-cancel nil}}
                              (registry/add extension-data)
                              (registry/activate extension-key)))))

(handlers/register-handler-db
 :extension/edit-address
 [re-frame/trim-v]
 (fn [db [address]]
   (assoc db :extension-url address)))

(handlers/register-handler-db
 :extension/stage
 [re-frame/trim-v]
 (fn [db [extension-data]]
   (-> db
       (assoc :staged-extension extension-data)
       (navigation/navigate-to :show-extension))))

(handlers/register-handler-fx
 :extension/show
 [re-frame/trim-v]
 (fn [cofx [uri]]
   {:extension/load [uri :extension/stage]}))

(handlers/register-handler-fx
 :extension/toggle-activation
 [re-frame/trim-v]
 (fn [cofx [id state]]
   (when-let [toggle-fn (get {true  registry/activate
                              false registry/deactivate}
                             state)]
     (toggle-fn id cofx))))
