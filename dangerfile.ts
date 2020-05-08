import { fail, message } from "danger";
import { promisify } from "util";
import { relative } from "path";

const exec = promisify(require("child_process").exec);

export default async () => {
  message(
    "Thank you for submitting a pull request! The team will review your submission as soon as possible."
  );

  await checkLinting("app/src/main/java/org/immuni/android/");
  await checkLinting("debugmenu/src/main/java/org/immuni/android");
  await checkLinting("extensions/src/main/java/org/immuni/android");
};

const checkLinting = async (path: string) => {
  let reports: ktlintReport[];
  let toolVersion: string;

  try {
    toolVersion = (
      await exec("ktlint --version")
    ).stdout.replace(/\s+/g, ' ').trim();

    const { stdout } = await exec(
      `ktlint --reporter=json || true`,
      { cwd: path, encoding: "utf8" }
    );

    reports = JSON.parse(stdout) as ktlintReport[];
    lintReportToDanger(path, reports, toolVersion);
  } catch (error) {
    // if there are errors, the exit code is not 0 and the exec fn throws
    const { killed, code } = error;

    if (killed || code != 2) {
      fail(
        `ktlint cannot be executed. This is a CI error killed: ${killed}, code: ${code}`
      );
    } else {
      reports = JSON.parse(error.stdout) as ktlintReport[];
      lintReportToDanger(path, reports, toolVersion);
    }
  }
};

const lintReportToDanger = (path: string, reports: ktlintReport[], toolVersion: string) => {
  if (reports.length == 0) {
    message(`:white_check_mark: ktlint passed`);
    return;
  }

  // ktlint only supports errors, not warnings
  for (const report of reports) {
    report.errors.forEach(function (error) {
      const reportMessage = `<br>**ktlint (${toolVersion}): path ${path}**<br>rule ${error.rule}<br>${error.message}`;
      const file = relative(path, report.file);
      fail(reportMessage, file, error.line);
    });
  }
};

// taken from https://github.com/pinterest/ktlint/blob/master/ktlint-reporter-json/src/main/kotlin/com/pinterest/ktlint/reporter/json/JsonReporter.kt
type ktlintError = {
    line: number;
    column: number;
    message: string;
    rule: string;
}
type ktlintReport = {
  file: string;
  errors: ktlintError[];
};
