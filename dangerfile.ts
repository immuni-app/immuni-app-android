import { fail, message } from "danger";
import { promisify } from "util";
import { relative } from "path";

const exec = promisify(require("child_process").exec);

export default async () => {
  message(
    "Thank you for submitting a pull request! The team will review your submission as soon as possible."
  );

  await checkLinting();
};

const checkLinting = async () => {
  let reports: ktlintReport[];

  try {
    const { stdout } = await exec(
      `./ktlint --reporter=json`,
      { encoding: "utf8" }
    );

    reports = JSON.parse(stdout) as ktlintReport[];
    lintReportToDanger(reports);
  } catch (error) {
    // if there are errors, the exit code is not 0 and the exec
    // fn throws
    const { killed, code } = error;

    if (killed || code != 2) {
      fail(
        `ktlint cannot be executed. This is a CI error killed: ${killed}, code: ${code}`
      );
    } else {
      reports = JSON.parse(error.stdout) as ktlintReport[];
      lintReportToDanger(reports);
    }
  }
};

const lintReportToDanger = (reports: ktlintReport[]) => {
  if (reports.length == 0) {
    message(`:white_check_mark: ktlint passed`);
    return;
  }

  const cwd = process.cwd();

  // ktlint only supports errors, not warnings
  for (const report of reports) {
    for (const error in report.errors) {
      const reportMessage = `${error.message} (${error.rule})`;
      const file = relative(cwd, report.file);
      fail(reportMessage, file, error.line);
    }
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
