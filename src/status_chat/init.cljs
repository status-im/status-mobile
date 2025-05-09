(ns status-chat.init
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.config :as config]
            [status-im.contexts.profile.rpc :as profile.rpc]))

(defn- reduce-profiles
  [profiles]
  (reduce
   (fn [acc {:keys [key-uid] :as profile}]
     (assoc acc key-uid (profile.rpc/rpc->profiles-overview profile)))
   {}
   profiles))

(defn get-profiles
  []
  (-> {:dataDir              (native-module/backup-disabled-data-dir)
       :mixpanelAppId        config/mixpanel-app-id
       :mixpanelToken        config/mixpanel-token
       :sentryDSN            (if config/sentry-enabled? config/sentry-dsn-status-go "")
       :mediaServerEnableTLS (config/enabled? config/STATUS_BACKEND_SERVER_MEDIA_SERVER_ENABLE_TLS)
       :logEnabled           (not (string/blank? (config/log-level)))
       :logLevel             (config/log-level)
       :logDir               (native-module/log-file-directory config/use-public-log-dir?)
       :apiLoggingEnabled    config/api-logging-enabled?}
      native-module/initialize-application
      :accounts
      reduce-profiles))

