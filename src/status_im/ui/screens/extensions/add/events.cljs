(ns status-im.ui.screens.extensions.add.events
  (:require [re-frame.core :as re-frame]
            [pluto.registry :as registry]
            [status-im.extensions.core :as extensions]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(re-frame/reg-fx
 :extension/load
 (fn [[url follow-up-event]]
   (extensions/load-from url #(re-frame/dispatch [follow-up-event (-> % extensions/read-extension extensions/parse)]))))

(handlers/register-handler-fx
 :extension/install
 (fn [cofx [_ extension-data]]
   (let [extension-key (get-in extension-data ['meta :name])]
     (fx/merge cofx
               {:ui/show-confirmation {:title     (i18n/label :t/success)
                                       :content   (i18n/label :t/extension-installed)
                                       :on-accept #(re-frame/dispatch [:navigate-to-clean :my-profile])
                                       :on-cancel nil}}
               #(registry/add extension-data %)
               #(registry/activate extension-key %)))))

(handlers/register-handler-fx
 :extension/edit-address
 (fn [{:keys [db]} [_ address]]
   {:db (assoc db :extension-url address)}))

(handlers/register-handler-fx
 :extension/stage
 (fn [{:keys [db] :as cofx} [_ extension-data]]
   (fx/merge cofx
             {:db (assoc db :staged-extension extension-data)}
             (navigation/navigate-to-cofx :show-extension nil))))

(handlers/register-handler-fx
 :extension/show
 (fn [cofx [_ uri]]
   {:extension/load [uri :extension/stage]}))

(handlers/register-handler-fx
 :extension/toggle-activation
 (fn [cofx [_ id state]]
   (when-let [toggle-fn (get {true  registry/activate
                              false registry/deactivate}
                             state)]
     (toggle-fn id cofx))))
