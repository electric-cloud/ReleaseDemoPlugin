use strict;
use ElectricCommander ();
$| = 1;
my $ec = new ElectricCommander->new();

my $artifactGroup = "$[_artifactGroup]";
my @artifactKeys = 	split(",","$[_artifactKeys]");
my @appTiers = 		split(",","$[_appTiers]");
my @versions = 		split(",","$[_versions]");

for (my $i=0 ; $i< scalar @artifactKeys ; $i++) {
	print $ec->createJobStep({
		jobStepName => "${artifactGroup}.@artifactKeys[$i]:@appTiers[$i]:@versions[$i]",
		parallel => 0,
		subproject => '/plugins/EC-Artifact/project',
		subprocedure => 'Publish',
		actualParameter => [
			{ actualParameterName => 'artifactName', 
				value => "${artifactGroup}.@artifactKeys[$i]:@appTiers[$i]" },
			{ actualParameterName => 'artifactVersionVersion',
				value => @versions[$i]},
			{ actualParameterName => 'fromLocation',
				value => '.'},
			{ actualParameterName => 'includePatterns',
				value => 'installer.sh'},
			{ actualParameterName => 'repositoryName',
				value => 'default'}
		]
	})->findnodes_as_string("/");
}