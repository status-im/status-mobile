(ns status-im.extensions.module
  (:require-macros [status-im.modules :as modules])
  (:require status-im.extensions.ui.db
            [pluto.storages :as storages]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [clojure.string :as string]
            [status-im.extensions.constants :as constants]))

(modules/defmodule extensions
  {:load                        'status-im.extensions.registry/load
   :valid-uri?                  'status-im.extensions.core/valid-uri?
   :parse-extension             'status-im.extensions.core/parse-extension
   :take-picture-view           'status-im.extensions.capacities.camera.views/take-picture
   :screen-holder-view          'status-im.extensions.capacities.views/screen-holder
   :extensions-settings-view    'status-im.extensions.ui.views/extensions-settings
   :selection-modal-screen-view 'status-im.extensions.ui.views/selection-modal-screen
   :edit-extension-view         'status-im.extensions.ui.add.views/edit-extension
   :show-extension-view         'status-im.extensions.ui.add.views/show-extension
   :show-extension-modal-view   'status-im.extensions.ui.add.views/show-extension-modal})

(defn load [& args]
  (apply (get-symbol :load) args))

(defn valid-uri? [& args]
  (apply (get-symbol :valid-uri?) args))

(defn parse-extension [& args]
  (apply (get-symbol :parse-extension) args))

(defn take-picture-view []
  [(get-symbol :take-picture-view)])

(defn screen-holder-view []
  [(get-symbol :screen-holder-view)])

(defn extensions-settings-view []
  [(get-symbol :extensions-settings-view)])

(defn selection-modal-screen-view []
  [(get-symbol :selection-modal-screen-view)])

(defn edit-extension-view []
  [(get-symbol :edit-extension-view)])

(defn show-extension-view []
  [(get-symbol :show-extension-view)])

(defn show-extension-modal-view []
  [(get-symbol :show-extension-modal-view)])

;; Initialization
;; Prevents loading of the rest of the module till it is necessary.

(defn url->uri [s]
  (when s
    (-> s
        (string/replace constants/uri-prefix "")
        (string/replace constants/link-prefix ""))))

(defn load-from [url f]
  (when-let [uri (url->uri url)]
    (storages/fetch uri f)))

(re-frame/reg-fx
 :extensions/load
 (fn [{:keys [extensions follow-up]}]
   (doseq [{:keys [url active?]} extensions]
     (load-from url #(re-frame/dispatch [follow-up url (parse-extension % url) active?])))))

(fx/defn initialize
  [{{:account/keys [account] :as db} :db}]
  (let [{:keys [extensions dev-mode?]} account
        ext-vals (vals extensions)]
    (when dev-mode?
      {:db              (assoc db :extensions/store (into {} (map (fn [{:keys [id data]}] {id data}) ext-vals)))
       :extensions/load {:extensions ext-vals
                         :follow-up  :extensions/add-to-registry}})))
