(ns status-im.activity-center.notification-types)

(def ^:const no-type 0)
(def ^:const one-to-one-chat 1)
(def ^:const private-group-chat 2)
(def ^:const mention 3)
(def ^:const reply 4)
(def ^:const contact-request 5)
(def ^:const contact-verification 10)

;; TODO: Remove this constant once the old Notification Center code is removed.
;; Its value clashes with the new constant `-contact-verification`
;; used in status-go.
(def ^:const contact-request-retracted 6)

;; TODO: Replace with correct enum values once status-go implements them.
(def ^:const admin 66610)
(def ^:const tx 66612)
(def ^:const membership 66613)
(def ^:const system 66614)
