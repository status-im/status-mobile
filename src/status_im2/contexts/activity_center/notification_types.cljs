(ns status-im2.contexts.activity-center.notification-types)

(def ^:const no-type 0)
(def ^:const one-to-one-chat 1)
(def ^:const private-group-chat 2)
(def ^:const mention 3)
(def ^:const reply 4)
(def ^:const contact-request 5)
(def ^:const admin 8)
(def ^:const contact-verification 10)

(def ^:const all-supported [one-to-one-chat
                            private-group-chat
                            mention
                            reply
                            contact-request
                            admin
                            contact-verification])

;; TODO: Replace with correct enum values once status-go implements them.
(def ^:const tx 66612)
(def ^:const system 66614)

(def ^:const membership
  "Membership is like a logical group of notifications with different types, i.e.
  it doesn't have a corresponding type in the backend. Think of the collection
  as a composite key of actual types."
  #{private-group-chat})
