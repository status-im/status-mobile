(ns status-im.models.browser
  (:require [status-im.data-store.browser :as browser-store]
            [status-im.data-store.dapp-permissions :as dapp-permissions]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]))

(defn get-current-url [{:keys [history history-index]}]
  (when (and history-index history)
    (nth history history-index)))

(defn can-go-back? [{:keys [history-index]}]
  (pos? history-index))

(defn can-go-forward? [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(defn update-browser-fx [{:keys [db now]} browser]
  (let [updated-browser (assoc browser :timestamp now)]
    {:db            (update-in db [:browser/browsers (:browser-id updated-browser)]
                               merge updated-browser)
     :data-store/tx [(browser-store/save-browser-tx updated-browser)]}))

(defn update-browser-history-fx [cofx browser url loading?]
  (when-not loading?
    (let [history-index (:history-index browser)
          history       (:history browser)
          history-url   (try (nth history history-index) (catch js/Error _))]
      (when (not= history-url url)
        (let [slash?      (= url (str history-url "/"))
              new-history (if slash?
                            (assoc history history-index url)
                            (conj (subvec history 0 (inc history-index)) url))
              new-index   (if slash?
                            history-index
                            (dec (count new-history)))]
          (update-browser-fx cofx
                             (assoc browser :history new-history :history-index new-index)))))))

(defn update-browser-and-navigate [cofx browser]
  (merge (update-browser-fx cofx browser)
         {:dispatch [:navigate-to :browser (:browser-id browser)]}))

(def permissions {constants/dapp-permission-contact-code {:label (i18n/label :t/your-contact-code)}})

(defn update-dapp-permissions-fx [{:keys [db]} permissions]
  {:db            (assoc-in db [:dapps/permissions (:dapp permissions)] permissions)
   :data-store/tx [(dapp-permissions/save-dapp-permissions permissions)]})

(defn request-permission [cofx
                          {:keys [dapp-name index requested-permissions permissions-allowed user-permissions
                                  permissions-data webview]
                           :as   params}]
  ;; iterate all requested permissions
  (if (< index (count requested-permissions))
    (let [requested-permission (get requested-permissions index)]
      ;; if requested permission exists and valid continue if not decline permission
      (if (and requested-permission (get permissions requested-permission))
        ;; if permission already allowed go to next, if not, show confirmation dialog
        (if ((set user-permissions) requested-permission)
          {:dispatch [:next-dapp-permission params requested-permission permissions-data]}
          {:show-dapp-permission-confirmation-fx [requested-permission params]})
        {:dispatch [:next-dapp-permission params]}))
    (assoc (update-dapp-permissions-fx cofx {:dapp        dapp-name
                                             :permissions (vec (set (concat (keys permissions-allowed)
                                                                            user-permissions)))})
           :send-to-bridge-fx [permissions-allowed webview])))

(defn next-permission [cofx params & [permission permissions-data]]
  (request-permission
   cofx
   (cond-> params
     true
     (update :index inc)

     (and permission permissions-data)
     (assoc-in [:permissions-allowed permission] (get permissions-data permission)))))