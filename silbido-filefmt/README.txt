Load/save tonal lists in the Silbido file format with Matlab

dtTonalsLoad - load tonals from file
dtTonalsSave - write tonals to file

Type help function-name for details on either function.

See the file ManipulatingTonals for further informtion on how to use
the tonals.


IMPORTANT:  For these functions to work, Matlab's java class path must
be set to point to the java directory contained in this distribution.
The javaaddpath function will allow this.

Example where the root level of this package is called silbido-filefmt
and is located in the user's matlab directory on a Windows 7 system:

% Get user's home directory for a Windows 7/Vista system
home = fullfile(getenv('HOMEDRIVE'), getenv('HOMEPATH'), 'Documents');
javaaddpath(fullfile(home, 'matlab', 'silbido-filefmt', 'java'));

Once this has been executed, the directories will be accessible to
Java for the current session.  Place the code in your Matlab
startup file if you want this to execute each time Matlab is started.

