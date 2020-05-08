import { message } from "danger";
import commitLint from "./ci/danger/commitlint";
import kotlinLint from "./ci/danger/ktlint";

export default async () => {
  message(
    "Thank you for submitting a pull request! The team will review your submission as soon as possible."
  );

  await commitLint({ enabled: true, allowedScopes: [] });

  await kotlinLint("app/src/main/java/org/immuni/android/");
  await kotlinLint("debugmenu/src/main/java/org/immuni/android");
  await kotlinLint("extensions/src/main/java/org/immuni/android");
};
