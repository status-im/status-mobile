(ns status-im.contexts.profile.settings.header.utils
  (:require [status-im.constants :as constants]
            [utils.i18n :as i18n]))

(defn visibility-status-type-data
  [{:keys [status-type]}]
  (condp = status-type
    constants/visibility-status-automatic
    {:status-title (i18n/label :t/status-automatic)
     :status-icon  :i/automatic}

    constants/visibility-status-always-online
    {:status-title (i18n/label :t/online)
     :status-icon  :i/online}

    constants/visibility-status-inactive
    {:status-title (i18n/label :t/offline)
     :status-icon  :i/offline}

    {:status-title (i18n/label :t/offline)
     :status-icon  :i/offline}))
